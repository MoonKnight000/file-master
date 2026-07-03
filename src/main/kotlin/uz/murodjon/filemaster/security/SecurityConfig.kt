package uz.murodjon.filemaster.security

import jakarta.servlet.DispatcherType
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import uz.murodjon.filemaster.auth.security.RestAccessDeniedHandler
import uz.murodjon.filemaster.auth.security.RestAuthenticationEntryPoint
import uz.murodjon.filemaster.security.TokenAuthenticationFilter
import uz.murodjon.filemaster.auth.service.AuthService
import uz.murodjon.filemaster.config.AppProperties

@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val authService: AuthService,
    private val props: AppProperties,
) {

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .cors { } // uses the corsConfigurationSource bean below
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests {
                // Async endpoints (SSE progress, zip streaming) re-dispatch as ASYNC; the
                // original REQUEST was already authorized, but the async dispatch has no
                // SecurityContext, so re-authorizing it would wrongly throw Access Denied.
                it.dispatcherTypeMatchers(
                    DispatcherType.ASYNC,
                    DispatcherType.FORWARD,
                    DispatcherType.ERROR,
                ).permitAll()
                // Public
                it.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                it.requestMatchers(HttpMethod.POST, "/v1/auth/**").permitAll()
                it.requestMatchers(HttpMethod.GET, "/v1/tools", "/v1/tools/**").permitAll()
                it.requestMatchers(HttpMethod.GET, "/v1/share/**").permitAll()
                // Payment provider callback — no bearer token; guarded by X-Webhook-Secret.
                it.requestMatchers(HttpMethod.POST, "/v1/billing/webhook").permitAll()
                it.requestMatchers(
                    "/v3/api-docs/**",
                    "/swagger-ui/**",
                    "/swagger-ui.html",
                    "/actuator/health",
                    "/actuator/health/**",
                ).permitAll()
                // Everything else requires a valid bearer token
                it.anyRequest().authenticated()
            }
            .exceptionHandling {
                it.authenticationEntryPoint(RestAuthenticationEntryPoint())
                it.accessDeniedHandler(RestAccessDeniedHandler())
            }
            .addFilterBefore(
                TokenAuthenticationFilter(authService),
                UsernamePasswordAuthenticationFilter::class.java,
            )
        return http.build()
    }

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val config = CorsConfiguration().apply {
            allowedOrigins = props.cors.origins().toList()
            allowedMethods = listOf("GET", "POST", "PATCH", "DELETE", "OPTIONS")
            allowedHeaders = listOf("*")
            exposedHeaders = listOf("Content-Disposition")
            allowCredentials = true
        }
        return UrlBasedCorsConfigurationSource().apply {
            registerCorsConfiguration("/**", config)
        }
    }
}
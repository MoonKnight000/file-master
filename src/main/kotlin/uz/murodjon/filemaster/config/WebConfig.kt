package uz.murodjon.filemaster.config

import org.springframework.context.annotation.Configuration
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import uz.murodjon.filemaster.auth.security.CurrentUserArgumentResolver

@Configuration
class WebConfig(
    private val currentUserArgumentResolver: CurrentUserArgumentResolver,
) : WebMvcConfigurer {

    // CORS is configured in SecurityConfig (CorsConfigurationSource) so the security
    // filter chain handles preflight requests correctly.

    override fun addArgumentResolvers(resolvers: MutableList<HandlerMethodArgumentResolver>) {
        resolvers.add(currentUserArgumentResolver)
    }
}

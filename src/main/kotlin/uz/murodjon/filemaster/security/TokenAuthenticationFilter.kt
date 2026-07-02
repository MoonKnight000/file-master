package uz.murodjon.filemaster.security

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpHeaders
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.web.filter.OncePerRequestFilter
import uz.murodjon.filemaster.auth.enums.UserPlan
import uz.murodjon.filemaster.auth.security.SessionCookies
import uz.murodjon.filemaster.auth.service.AuthService

/**
 * Resolves the session token from either the `Authorization: Bearer <token>` header
 * (checked first, for Swagger/API clients) or the `fm_session` cookie (for browsers).
 * If it resolves to a valid session, populates the
 * [org.springframework.security.core.context.SecurityContextHolder] with the
 * [uz.murodjon.filemaster.auth.model.User] as the principal. Invalid/missing tokens are left
 * unauthenticated — protected routes are then rejected by the security chain's entry point.
 */
class TokenAuthenticationFilter(
    private val authService: AuthService,
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val token = resolveToken(request)
        if (token != null && SecurityContextHolder.getContext().authentication == null) {
            runCatching { authService.requireUser(token) }
                .onSuccess { user ->
                    val authorities = buildList {
                        add(SimpleGrantedAuthority("ROLE_USER"))
                        if (user.plan == UserPlan.PREMIUM) add(SimpleGrantedAuthority("ROLE_PRO"))
                    }
                    val authentication = UsernamePasswordAuthenticationToken(user, null, authorities)
                    authentication.details = WebAuthenticationDetailsSource().buildDetails(request)
                    SecurityContextHolder.getContext().authentication = authentication
                }
                .onFailure { SecurityContextHolder.clearContext() }
        }
        filterChain.doFilter(request, response)
    }

    /** Bearer header first (Swagger/API), then the `fm_session` cookie (browser). */
    private fun resolveToken(request: HttpServletRequest): String? {
        request.getHeader(HttpHeaders.AUTHORIZATION)?.takeIf { it.isNotBlank() }?.let { return it }
        return request.cookies?.firstOrNull { it.name == SessionCookies.SESSION }?.value?.takeIf { it.isNotBlank() }
    }
}
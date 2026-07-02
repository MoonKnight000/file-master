package uz.murodjon.filemaster.auth.security

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint

/** Returns the app's standard error envelope (instead of Spring's default) for 401s. */
class RestAuthenticationEntryPoint : AuthenticationEntryPoint {

    override fun commence(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authException: AuthenticationException,
    ) {
        val body = """{"error":{"code":"UNAUTHENTICATED","message":"Authentication required."}}"""
            .toByteArray(Charsets.UTF_8)
        response.status = HttpStatus.UNAUTHORIZED.value()
        response.contentType = MediaType.APPLICATION_JSON_VALUE
        response.characterEncoding = Charsets.UTF_8.name()
        response.setContentLength(body.size)
        response.outputStream.write(body)
        response.outputStream.flush()
    }
}

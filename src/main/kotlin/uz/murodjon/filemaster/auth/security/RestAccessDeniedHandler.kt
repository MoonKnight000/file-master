package uz.murodjon.filemaster.auth.security

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.web.access.AccessDeniedHandler

/** Returns the app's standard error envelope (instead of Spring's default) for 403s. */
class RestAccessDeniedHandler : AccessDeniedHandler {

    override fun handle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        accessDeniedException: AccessDeniedException,
    ) {
        val body = """{"error":{"code":"FORBIDDEN","message":"Access denied."}}"""
            .toByteArray(Charsets.UTF_8)
        response.status = HttpStatus.FORBIDDEN.value()
        response.contentType = MediaType.APPLICATION_JSON_VALUE
        response.characterEncoding = Charsets.UTF_8.name()
        response.setContentLength(body.size)
        response.outputStream.write(body)
        response.outputStream.flush()
    }
}

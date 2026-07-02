package uz.murodjon.filemaster.auth.controller

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController
import uz.murodjon.filemaster.auth.dto.GoogleSignInRequest
import uz.murodjon.filemaster.auth.dto.IssuedSession
import uz.murodjon.filemaster.auth.dto.LoginRequest
import uz.murodjon.filemaster.auth.dto.RegisterRequest
import uz.murodjon.filemaster.auth.dto.SessionRequest
import uz.murodjon.filemaster.auth.dto.SessionResponse
import uz.murodjon.filemaster.auth.dto.SessionUser
import uz.murodjon.filemaster.auth.security.SessionCookies
import uz.murodjon.filemaster.auth.service.AuthService
import uz.murodjon.filemaster.util.ResponseData
import java.time.Instant

@RestController
class AuthControllerImpl(
    private val authService: AuthService,
) : AuthController {

    override fun createSession(
        body: SessionRequest?,
        response: HttpServletResponse,
    ): ResponseEntity<ResponseData<SessionResponse>> {
        val issued = authService.createGuestSession()
        writeSessionCookies(response, issued)
        return ResponseEntity.status(HttpStatus.CREATED).body(ResponseData(issued.toResponse()))
    }

    override fun register(
        request: RegisterRequest,
        response: HttpServletResponse,
    ): ResponseEntity<ResponseData<SessionResponse>> {
        val issued = authService.register(request)
        writeSessionCookies(response, issued)
        return ResponseEntity.status(HttpStatus.CREATED).body(ResponseData(issued.toResponse()))
    }

    override fun login(
        request: LoginRequest,
        response: HttpServletResponse,
    ): ResponseEntity<ResponseData<SessionResponse>> {
        val issued = authService.login(request)
        writeSessionCookies(response, issued)
        return ResponseEntity.ok(ResponseData(issued.toResponse()))
    }

    override fun google(
        request: GoogleSignInRequest,
        response: HttpServletResponse,
    ): ResponseEntity<ResponseData<SessionResponse>> {
        val issued = authService.loginWithGoogle(request)
        writeSessionCookies(response, issued)
        return ResponseEntity.ok(ResponseData(issued.toResponse()))
    }

    override fun logout(
        request: HttpServletRequest,
        response: HttpServletResponse,
    ): ResponseEntity<ResponseData<Unit>> {
        authService.logout(tokenFrom(request))
        SessionCookies.clear().forEach { response.addHeader(HttpHeaders.SET_COOKIE, it.toString()) }
        return ResponseEntity.ok(ResponseData(message = "Logged out"))
    }

    private fun writeSessionCookies(response: HttpServletResponse, issued: IssuedSession) {
        val ttl = (issued.expiresTimestamp - Instant.now().epochSecond).coerceAtLeast(0)
        SessionCookies.forUser(issued.token, ttl, issued.user.guest).forEach {
            response.addHeader(HttpHeaders.SET_COOKIE, it.toString())
        }
    }

    private fun IssuedSession.toResponse() = SessionResponse(
        token = token,
        expiresTimestamp = expiresTimestamp,
        user = SessionUser(user.id!!, user.guest),
    )

    /** Token from the Authorization header, else the `fm_session` cookie. */
    private fun tokenFrom(request: HttpServletRequest): String? {
        request.getHeader(HttpHeaders.AUTHORIZATION)?.takeIf { it.isNotBlank() }?.let { return it }
        return request.cookies?.firstOrNull { it.name == SessionCookies.SESSION }?.value
    }
}

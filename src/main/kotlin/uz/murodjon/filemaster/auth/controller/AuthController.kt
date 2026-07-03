package uz.murodjon.filemaster.auth.controller

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import uz.murodjon.filemaster.auth.dto.GoogleSignInRequest
import uz.murodjon.filemaster.auth.dto.LoginRequest
import uz.murodjon.filemaster.auth.dto.RegisterRequest
import uz.murodjon.filemaster.auth.dto.SessionRequest
import uz.murodjon.filemaster.auth.dto.SessionResponse
import uz.murodjon.filemaster.util.ResponseData

@RequestMapping("/v1/auth")
interface AuthController {

    @PostMapping("/session")
    fun createSession(
        @RequestBody(required = false) body: SessionRequest?,
        response: HttpServletResponse,
    ): ResponseEntity<ResponseData<SessionResponse>>

    @PostMapping("/register")
    fun register(
        @RequestBody @Valid request: RegisterRequest,
        response: HttpServletResponse,
    ): ResponseEntity<ResponseData<SessionResponse>>

    @PostMapping("/login")
    fun login(
        @RequestBody request: LoginRequest,
        response: HttpServletResponse,
    ): ResponseEntity<ResponseData<SessionResponse>>

    @PostMapping("/google")
    fun google(
        @RequestBody @Valid request: GoogleSignInRequest,
        response: HttpServletResponse,
    ): ResponseEntity<ResponseData<SessionResponse>>

    @PostMapping("/logout")
    fun logout(
        request: HttpServletRequest,
        response: HttpServletResponse,
    ): ResponseEntity<ResponseData<Unit>>
}

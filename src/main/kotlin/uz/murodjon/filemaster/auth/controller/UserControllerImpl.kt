package uz.murodjon.filemaster.auth.controller

import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController
import uz.murodjon.filemaster.auth.dto.ChangePasswordRequest
import uz.murodjon.filemaster.auth.dto.UserResponse
import uz.murodjon.filemaster.auth.dto.UpdateProfileRequest
import uz.murodjon.filemaster.auth.model.User
import uz.murodjon.filemaster.auth.security.SessionCookies
import uz.murodjon.filemaster.auth.service.AuthService
import uz.murodjon.filemaster.tools.dto.FavoriteToggleResponse
import uz.murodjon.filemaster.tools.dto.UserToolsResponse
import uz.murodjon.filemaster.tools.service.UserToolStatService
import uz.murodjon.filemaster.util.ResponseData

@RestController
class UserControllerImpl(
    private val authService: AuthService
) : UserController {

    override fun me(user: User): ResponseEntity<ResponseData<UserResponse>> =
        ResponseEntity.ok(ResponseData(authService.me(user)))

    override fun updateProfile(
        user: User,
        request: UpdateProfileRequest,
    ): ResponseEntity<ResponseData<UserResponse>> =
        ResponseEntity.ok(ResponseData(authService.updateProfile(user, request), "Profile updated"))

    override fun changePassword(
        user: User,
        request: ChangePasswordRequest,
    ): ResponseEntity<ResponseData<Unit>> {
        authService.changePassword(user, request)
        return ResponseEntity.ok(ResponseData(message = "Password changed"))
    }

    override fun deleteAccount(
        user: User,
        response: HttpServletResponse,
    ): ResponseEntity<ResponseData<Unit>> {
        authService.deleteAccount(user)
        SessionCookies.clear().forEach { response.addHeader(HttpHeaders.SET_COOKIE, it.toString()) }
        return ResponseEntity.ok(ResponseData(message = "Account deleted"))
    }


}

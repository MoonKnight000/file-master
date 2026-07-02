package uz.murodjon.filemaster.auth.controller

import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import uz.murodjon.filemaster.auth.dto.ChangePasswordRequest
import uz.murodjon.filemaster.auth.dto.UserResponse
import uz.murodjon.filemaster.auth.dto.UpdateProfileRequest
import uz.murodjon.filemaster.auth.model.User
import uz.murodjon.filemaster.auth.security.CurrentUser
import uz.murodjon.filemaster.util.ResponseData

@RequestMapping("/v1/user")
interface UserController {

    @GetMapping
    fun me(@CurrentUser user: User): ResponseEntity<ResponseData<UserResponse>>

    @PatchMapping
    fun updateProfile(
        @CurrentUser user: User,
        @RequestBody request: UpdateProfileRequest,
    ): ResponseEntity<ResponseData<UserResponse>>

    @PostMapping("/password")
    fun changePassword(
        @CurrentUser user: User,
        @RequestBody request: ChangePasswordRequest,
    ): ResponseEntity<ResponseData<Unit>>

    @DeleteMapping
    fun deleteAccount(
        @CurrentUser user: User,
        response: HttpServletResponse,
    ): ResponseEntity<ResponseData<Unit>>

}
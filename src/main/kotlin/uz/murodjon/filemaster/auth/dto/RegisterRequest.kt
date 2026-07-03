package uz.murodjon.filemaster.auth.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size

data class RegisterRequest(
    @field:NotBlank(message = "A valid email is required")
    @field:Email(message = "A valid email is required")
    val email: String?,
    @field:NotNull(message = "Password must be at least 6 characters")
    @field:Size(min = 6, message = "Password must be at least 6 characters")
    val password: String?,
    val name: String?,
)

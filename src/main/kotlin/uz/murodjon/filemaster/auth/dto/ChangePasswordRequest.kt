package uz.murodjon.filemaster.auth.dto

import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size

data class ChangePasswordRequest(
    @field:NotNull(message = "Current password is required")
    val currentPassword: String? = null,
    @field:NotNull(message = "New password must be at least 6 characters")
    @field:Size(min = 6, message = "New password must be at least 6 characters")
    val newPassword: String? = null,
)

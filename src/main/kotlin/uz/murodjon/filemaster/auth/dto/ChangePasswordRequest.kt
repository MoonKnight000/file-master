package uz.murodjon.filemaster.auth.dto

data class ChangePasswordRequest(
    val currentPassword: String? = null,
    val newPassword: String? = null,
)

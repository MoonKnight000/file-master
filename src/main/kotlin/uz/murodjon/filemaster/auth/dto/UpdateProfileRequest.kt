package uz.murodjon.filemaster.auth.dto

data class UpdateProfileRequest(
    val name: String? = null,
    val avatarId: Long? = null,
)

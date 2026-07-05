package uz.murodjon.filemaster.auth.dto

data class SessionResponse(
    val token: String,
    val expiresTimestamp: Long,
    val refreshToken: String,
    val refreshExpiresTimestamp: Long,
    val user: SessionUser,
)

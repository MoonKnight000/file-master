package uz.murodjon.filemaster.auth.dto

data class SessionResponse(val token: String, val expiresTimestamp: Long, val user: SessionUser)

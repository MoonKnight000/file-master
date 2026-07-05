package uz.murodjon.filemaster.auth.dto

import uz.murodjon.filemaster.auth.model.User

/** A freshly issued token pair with the RAW token values (only ever returned, never stored). */
class IssuedSession(
    val token: String,
    val expiresTimestamp: Long,
    val refreshToken: String,
    val refreshExpiresTimestamp: Long,
    val user: User,
)

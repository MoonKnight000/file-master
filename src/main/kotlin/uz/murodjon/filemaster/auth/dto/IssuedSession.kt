package uz.murodjon.filemaster.auth.dto

import uz.murodjon.filemaster.auth.model.User

class IssuedSession(val token: String, val expiresTimestamp: Long, val user: User)

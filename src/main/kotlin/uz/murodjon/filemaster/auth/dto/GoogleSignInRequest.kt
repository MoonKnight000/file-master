package uz.murodjon.filemaster.auth.dto

import jakarta.validation.constraints.NotBlank

/** Body of `POST /v1/auth/google`: the Google ID token obtained client-side (Google Identity Services). */
data class GoogleSignInRequest(
    @field:NotBlank(message = "`idToken` is required")
    val idToken: String?,
)

package uz.murodjon.filemaster.auth.dto

/** Body of `POST /v1/auth/google`: the Google ID token obtained client-side (Google Identity Services). */
data class GoogleSignInRequest(val idToken: String?)

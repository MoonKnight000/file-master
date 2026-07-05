package uz.murodjon.filemaster.auth.dto

/** Body of `POST /v1/auth/refresh`; the token may also come from the `fm_refresh` cookie. */
data class RefreshRequest(
    val refreshToken: String? = null,
)

package uz.murodjon.filemaster.auth.security

import org.springframework.http.ResponseCookie

/** Cookie names + builders for the session/refresh tokens and the guest flag. */
object SessionCookies {
    /** HttpOnly cookie carrying the access token (browser auto-sends it; JS can't read it). */
    const val SESSION = "fm_session"

    /** HttpOnly cookie carrying the refresh token (read by `POST /v1/auth/refresh`). */
    const val REFRESH = "fm_refresh"

    /** Readable flag the front-end can use to know whether the user is a guest. */
    const val GUEST = "fm_guest"

    /**
     * Cookies for a session: HttpOnly access + refresh token cookies + the readable guest
     * flag. The guest flag lives as long as the REFRESH window (the session is renewable
     * for that long, so the "who am I" hint shouldn't vanish with the short access token).
     *
     * NOTE: `secure = false` and `SameSite = Lax` suit local http dev (front & back share the
     * `localhost` site). In production over https with a cross-site front-end, switch to
     * `secure = true` and `SameSite = None`.
     */
    fun forUser(
        token: String,
        ttlSeconds: Long,
        refreshToken: String,
        refreshTtlSeconds: Long,
        guest: Boolean,
    ): List<ResponseCookie> = listOf(
        ResponseCookie.from(SESSION, token)
            .httpOnly(true).path("/").sameSite("Lax").maxAge(ttlSeconds).secure(false).build(),
        ResponseCookie.from(REFRESH, refreshToken)
            .httpOnly(true).path("/").sameSite("Lax").maxAge(refreshTtlSeconds).secure(false).build(),
        ResponseCookie.from(GUEST, guest.toString())
            .httpOnly(false).path("/").sameSite("Lax").maxAge(refreshTtlSeconds).secure(false).build(),
    )

    /** Expired cookies that delete the session, refresh and guest flag (for logout). */
    fun clear(): List<ResponseCookie> = listOf(
        ResponseCookie.from(SESSION, "").httpOnly(true).path("/").sameSite("Lax").maxAge(0).secure(false).build(),
        ResponseCookie.from(REFRESH, "").httpOnly(true).path("/").sameSite("Lax").maxAge(0).secure(false).build(),
        ResponseCookie.from(GUEST, "").httpOnly(false).path("/").sameSite("Lax").maxAge(0).secure(false).build(),
    )
}

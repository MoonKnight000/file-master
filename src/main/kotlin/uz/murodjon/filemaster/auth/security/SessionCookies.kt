package uz.murodjon.filemaster.auth.security

import org.springframework.http.ResponseCookie

/** Cookie names + builders for the session token and the guest flag. */
object SessionCookies {
    /** HttpOnly cookie carrying the session token (browser auto-sends it; JS can't read it). */
    const val SESSION = "fm_session"

    /** Readable flag the front-end can use to know whether the user is a guest. */
    const val GUEST = "fm_guest"

    /**
     * Cookies for a session: the HttpOnly token cookie + the readable guest flag.
     *
     * NOTE: `secure = false` and `SameSite = Lax` suit local http dev (front & back share the
     * `localhost` site). In production over https with a cross-site front-end, switch to
     * `secure = true` and `SameSite = None`.
     */
    fun forUser(token: String, ttlSeconds: Long, guest: Boolean): List<ResponseCookie> = listOf(
        ResponseCookie.from(SESSION, token)
            .httpOnly(true).path("/").sameSite("Lax").maxAge(ttlSeconds).secure(false).build(),
        ResponseCookie.from(GUEST, guest.toString())
            .httpOnly(false).path("/").sameSite("Lax").maxAge(ttlSeconds).secure(false).build(),
    )

    /** Expired cookies that delete the session + guest flag (for logout). */
    fun clear(): List<ResponseCookie> = listOf(
        ResponseCookie.from(SESSION, "").httpOnly(true).path("/").sameSite("Lax").maxAge(0).secure(false).build(),
        ResponseCookie.from(GUEST, "").httpOnly(false).path("/").sameSite("Lax").maxAge(0).secure(false).build(),
    )
}

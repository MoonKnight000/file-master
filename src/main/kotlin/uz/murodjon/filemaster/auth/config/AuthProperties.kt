package uz.murodjon.filemaster.auth.config

/** Session token lifetimes (`app.auth.*`). */
data class AuthProperties(
    /** Access (bearer) token TTL — short-lived; the front refreshes when it expires. */
    val accessTtlMinutes: Long = 60,
    /** Refresh token TTL. Rotation renews this window, so active users never re-login. */
    val refreshTtlDays: Long = 30,
) {
    val accessTtlSeconds: Long get() = accessTtlMinutes * 60
    val refreshTtlSeconds: Long get() = refreshTtlDays * 24 * 60 * 60
}

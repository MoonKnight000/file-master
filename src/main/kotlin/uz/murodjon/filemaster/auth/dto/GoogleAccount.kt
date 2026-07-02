package uz.murodjon.filemaster.auth.dto

/** A verified Google identity, distilled from the tokeninfo response after all checks pass. */
data class GoogleAccount(
    val googleId: String,
    val email: String,
    val name: String?,
    val avatarUrl: String?,
)

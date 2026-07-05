package uz.murodjon.filemaster.auth.config

/** Google Sign-In config. [clientId] is the OAuth 2.0 Web client id every ID token must be issued for. */
data class GoogleProperties(
    val clientId: String = "",
)

package uz.murodjon.filemaster.security.config

data class CorsProperties(
    /** Comma-separated list of allowed origins. */
    val allowedOrigins: String,
) {
    fun origins(): Array<String> =
        allowedOrigins.split(",").map { it.trim() }.filter { it.isNotEmpty() }.toTypedArray()
}

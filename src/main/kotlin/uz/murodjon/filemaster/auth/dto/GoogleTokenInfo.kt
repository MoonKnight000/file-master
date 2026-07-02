package uz.murodjon.filemaster.auth.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

/** Raw payload of Google's `tokeninfo` endpoint (Google already validated the signature + expiry). */
@JsonIgnoreProperties(ignoreUnknown = true)
data class GoogleTokenInfo(
    val iss: String? = null,
    val aud: String? = null,
    val sub: String? = null,
    val email: String? = null,
    @JsonProperty("email_verified") val emailVerified: String? = null,
    val name: String? = null,
    val picture: String? = null,
)

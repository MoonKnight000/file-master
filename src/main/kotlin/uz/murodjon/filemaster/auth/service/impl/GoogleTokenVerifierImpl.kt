package uz.murodjon.filemaster.auth.service.impl

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient
import uz.murodjon.filemaster.auth.dto.GoogleAccount
import uz.murodjon.filemaster.auth.dto.GoogleTokenInfo
import uz.murodjon.filemaster.auth.service.GoogleTokenVerifier
import uz.murodjon.filemaster.config.AppProperties
import uz.murodjon.filemaster.exception.UnauthenticatedException

/**
 * Verifies Google ID tokens via Google's `tokeninfo` endpoint, which validates the signature and
 * expiry server-side. We additionally enforce that the token was issued for our own client id
 * ([AppProperties.google]), comes from Google, and carries a verified email.
 */
@Service
class GoogleTokenVerifierImpl(
    private val props: AppProperties,
) : GoogleTokenVerifier {

    private val log = LoggerFactory.getLogger(javaClass)
    private val client = RestClient.create()

    override fun verify(idToken: String): GoogleAccount {
        val clientId = props.google.clientId.takeIf { it.isNotBlank() }
            ?: throw UnauthenticatedException("Google sign-in is not configured.")

        val info = runCatching {
            client.get()
                .uri("https://oauth2.googleapis.com/tokeninfo?id_token={t}", idToken)
                .retrieve()
                .body(GoogleTokenInfo::class.java)
        }.getOrElse {
            log.debug("Google tokeninfo rejected the token: {}", it.message)
            throw UnauthenticatedException("Invalid Google token.")
        } ?: throw UnauthenticatedException("Invalid Google token.")

        if (info.aud != clientId) {
            throw UnauthenticatedException("Google token was not issued for this app.")
        }
        if (info.iss != "accounts.google.com" && info.iss != "https://accounts.google.com") {
            throw UnauthenticatedException("Invalid Google token issuer.")
        }
        if (info.emailVerified != "true" || info.email.isNullOrBlank()) {
            throw UnauthenticatedException("Google account email is not verified.")
        }
        val sub = info.sub?.takeIf { it.isNotBlank() }
            ?: throw UnauthenticatedException("Invalid Google token.")

        return GoogleAccount(
            googleId = sub,
            email = info.email.trim().lowercase(),
            name = info.name?.trim()?.takeIf { it.isNotEmpty() },
            avatarUrl = info.picture?.takeIf { it.isNotBlank() },
        )
    }
}

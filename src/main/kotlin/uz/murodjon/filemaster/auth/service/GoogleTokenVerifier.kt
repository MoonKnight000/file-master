package uz.murodjon.filemaster.auth.service

import uz.murodjon.filemaster.auth.dto.GoogleAccount

/** Verifies a Google ID token and returns the identity behind it, or throws if it's invalid. */
interface GoogleTokenVerifier {
    fun verify(idToken: String): GoogleAccount
}

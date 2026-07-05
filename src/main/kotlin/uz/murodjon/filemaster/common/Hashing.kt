package uz.murodjon.filemaster.common

import java.security.MessageDigest

/** Digest helpers for secrets stored at rest (session tokens are kept only as hashes). */
object Hashing {
    /** Hex SHA-256 — deterministic, so the hash column stays directly indexable. */
    fun sha256Hex(value: String): String =
        MessageDigest.getInstance("SHA-256")
            .digest(value.toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it) }
}

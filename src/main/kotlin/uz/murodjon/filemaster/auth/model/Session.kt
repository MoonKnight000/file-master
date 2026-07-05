package uz.murodjon.filemaster.auth.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

/**
 * One issued access+refresh token pair. Raw tokens are NEVER stored — only SHA-256 hex
 * hashes, so a DB dump doesn't leak usable credentials. Refresh rotation overwrites both
 * hashes in place, which also invalidates the previous pair immediately.
 */
@Entity
@Table(name = "sessions")
class Session(
    @Column(unique = true, length = 64) var tokenHash: String,
    @Column(unique = true, length = 64) var refreshTokenHash: String,
    @ManyToOne(fetch = FetchType.LAZY) var user: User,
    /** Access token expiry (epoch seconds). */
    var expiresTimestamp: Long,
    /** Refresh token expiry — the whole row is swept once this passes. */
    var refreshExpiresTimestamp: Long,
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null
}

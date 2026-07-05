package uz.murodjon.filemaster.auth.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import uz.murodjon.filemaster.auth.model.Session
import java.util.Optional

@Repository
interface SessionRepository : JpaRepository<Session, Long> {
    fun findByTokenHash(tokenHash: String): Optional<Session>

    fun findByRefreshTokenHash(refreshTokenHash: String): Optional<Session>

    /** Ends every session of a user (used when deleting the account). */
    fun deleteByUserId(userId: Long): Long

    /** Sweeps rows whose refresh window has passed — used by the hourly session cleanup. */
    fun deleteByRefreshExpiresTimestampLessThan(cutoff: Long): Long
}

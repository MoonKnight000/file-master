package uz.murodjon.filemaster.auth.schedule

import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uz.murodjon.filemaster.auth.repository.SessionRepository
import java.time.Instant

/**
 * Deletes session rows whose REFRESH window has passed (hourly, 3 min after startup).
 * Rows with only an expired access token are kept — they are still refreshable.
 */
@Service
class SessionCleanupSchedule(private val sessions: SessionRepository) {

    private val log = LoggerFactory.getLogger(javaClass)

    @Scheduled(fixedDelay = 60 * 60 * 1000, initialDelay = 3 * 60 * 1000)
    @Transactional
    fun cleanupExpiredSessions() {
        val removed = sessions.deleteByRefreshExpiresTimestampLessThan(Instant.now().epochSecond)
        if (removed > 0) log.info("Sessions: removed {} expired session(s).", removed)
    }
}

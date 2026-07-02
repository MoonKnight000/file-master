package uz.murodjon.filemaster.files.schedule

import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uz.murodjon.filemaster.config.AppProperties
import uz.murodjon.filemaster.files.enums.FileSource
import uz.murodjon.filemaster.files.repository.StoredFileRepository
import uz.murodjon.filemaster.storage.StorageService
import java.time.Instant

@Service
class RetentionSchedule(
    private val files: StoredFileRepository,
    private val storage: StorageService,
    private val props: AppProperties,
) {

    private val log = LoggerFactory.getLogger(javaClass)

    /** Every 10 minutes (1 min after startup). Results live for `app.limits.retention-minutes`. */
    @Scheduled(fixedDelay = 10 * 60 * 1000, initialDelay = 60 * 1000)
    @Transactional
    fun cleanupExpiredResults() {
        val cutoff = Instant.now().epochSecond - props.limits.retentionMinutes * 60
        val expired = files.findBySourceAndActiveTrueAndCreatedTimestampLessThan(FileSource.RESULT, cutoff)
        if (expired.isEmpty()) return

        val now = Instant.now().epochSecond
        expired.forEach { file ->
            runCatching { storage.delete(file.absolutePath) }
                .onFailure { log.warn("Retention: failed to delete object {}: {}", file.absolutePath, it.message) }
            file.active = false
            file.deletedTimestamp = now
        }
        files.saveAll(expired)
        log.info("Retention: removed {} expired result file(s).", expired.size)
    }
}
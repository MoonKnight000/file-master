package uz.murodjon.filemaster.conversion.schedule

import org.slf4j.LoggerFactory
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uz.murodjon.filemaster.common.JobStatus
import uz.murodjon.filemaster.config.AppProperties
import uz.murodjon.filemaster.conversion.model.ConversionJob
import uz.murodjon.filemaster.conversion.processor.ConversionSupport
import uz.murodjon.filemaster.conversion.repository.ConversionJobRepository
import java.nio.file.Files
import java.nio.file.Paths
import java.time.Instant
import kotlin.io.path.deleteRecursively
import kotlin.io.path.getLastModifiedTime
import kotlin.io.path.listDirectoryEntries

/**
 * Fails conversion jobs whose worker can no longer finish them. The worker queue is
 * in-memory (@Async), so after a restart every QUEUED/PROCESSING job is a zombie — without
 * this sweep it would sit at "processing" forever in the UI. A periodic reaper also fails
 * jobs stuck longer than `app.limits.stale-job-minutes` (e.g. a hung external tool), and
 * scratch dirs left behind by the previous process are removed on startup.
 */
@Service
class StaleJobSchedule(
    private val jobs: ConversionJobRepository,
    private val support: ConversionSupport,
    private val props: AppProperties,
) {

    private val log = LoggerFactory.getLogger(javaClass)
    private val busy = listOf(JobStatus.QUEUED, JobStatus.PROCESSING)

    @EventListener(ApplicationReadyEvent::class)
    @Transactional
    fun failZombieJobsOnStartup() {
        val zombies = jobs.findByStatusIn(busy)
        if (zombies.isNotEmpty()) {
            zombies.forEach { reap(it, "The server was restarted while this conversion was running. Please retry.") }
            log.info("Stale jobs: failed {} job(s) orphaned by the previous run.", zombies.size)
        }
        cleanupOrphanedWorkDirs()
    }

    /** Every 10 minutes (2 min after startup): fail jobs stuck past the stale window. */
    @Scheduled(fixedDelay = 10 * 60 * 1000, initialDelay = 2 * 60 * 1000)
    @Transactional
    fun failStaleJobs() {
        val cutoff = Instant.now().epochSecond - props.limits.staleJobMinutes * 60
        val stale = jobs.findByStatusInAndCreatedTimestampLessThan(busy, cutoff)
        if (stale.isEmpty()) return

        stale.forEach { reap(it, "The conversion took too long and was cancelled. Please retry.") }
        log.info("Stale jobs: failed {} job(s) stuck longer than {} min.", stale.size, props.limits.staleJobMinutes)
    }

    /** Fails the job but keeps per-file DONE results intact (unlike a full failJob). */
    private fun reap(job: ConversionJob, message: String) {
        job.status = JobStatus.FAILED
        job.progress = 100
        job.files
            .filter { it.status == JobStatus.QUEUED || it.status == JobStatus.PROCESSING }
            .forEach {
                it.status = JobStatus.FAILED
                it.progress = 100
                it.error = message
            }
        jobs.save(job)
        support.emitDone(job)
    }

    /**
     * Scratch dirs are per-job and deleted by the worker when it finishes; anything still in
     * the work dir at startup belongs to the dead previous process. Only entries last modified
     * before this sweep are touched, so a job submitted right after startup is never hit.
     */
    @OptIn(kotlin.io.path.ExperimentalPathApi::class)
    private fun cleanupOrphanedWorkDirs() {
        val startedAt = Instant.now()
        val workDir = Paths.get(props.tools.workDir)
        if (!Files.isDirectory(workDir)) return

        val orphans = workDir.listDirectoryEntries()
            .filter { runCatching { it.getLastModifiedTime().toInstant().isBefore(startedAt) }.getOrDefault(false) }
        orphans.forEach { entry ->
            runCatching { entry.deleteRecursively() }
                .onFailure { log.warn("Stale jobs: could not delete scratch entry {}: {}", entry, it.message) }
        }
        if (orphans.isNotEmpty()) log.info("Stale jobs: removed {} orphaned scratch entr(y/ies).", orphans.size)
    }
}

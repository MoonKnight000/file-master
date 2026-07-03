package uz.murodjon.filemaster.conversion.processor

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import uz.murodjon.filemaster.common.FileCategories
import uz.murodjon.filemaster.common.Ids
import uz.murodjon.filemaster.common.JobStatus
import uz.murodjon.filemaster.config.AppProperties
import uz.murodjon.filemaster.conversion.dto.JobDto
import uz.murodjon.filemaster.conversion.engine.ConversionSettings
import uz.murodjon.filemaster.conversion.engine.Converter
import uz.murodjon.filemaster.conversion.model.ConversionJob
import uz.murodjon.filemaster.conversion.model.JobFile
import uz.murodjon.filemaster.conversion.repository.ConversionJobRepository
import uz.murodjon.filemaster.conversion.service.JobEvents
import uz.murodjon.filemaster.files.enums.FileSource
import uz.murodjon.filemaster.files.model.StoredFile
import uz.murodjon.filemaster.files.repository.StoredFileRepository
import uz.murodjon.filemaster.mail.service.MailService
import uz.murodjon.filemaster.storage.StorageService
import java.nio.file.Files
import java.nio.file.Path
import java.time.Instant
import java.util.UUID

/**
 * Shared, file-type-agnostic plumbing every [ConversionProcessor] reuses: job persistence,
 * progress/done events, result storage, and the standard 1->1 per-file conversion loop. This
 * is intentionally the *only* common surface, so type-specific logic stays in the processors.
 */
@Component
class ConversionSupport(
    val jobs: ConversionJobRepository,
    val files: StoredFileRepository,
    val storage: StorageService,
    private val events: JobEvents,
    private val mail: MailService,
    val props: AppProperties,
) {

    private val log = LoggerFactory.getLogger(javaClass)

    fun emitProgress(job: ConversionJob) = events.emitProgress(job.id!!, JobDto(job))

    fun emitDone(job: ConversionJob) {
        events.emitDone(job.id!!, JobDto(job))
        notifyByMail(job)
    }

    /** Best-effort "conversion ready" mail for registered users; never fails the job. */
    private fun notifyByMail(job: ConversionJob) {
        if (job.status != JobStatus.DONE) return
        val email = job.user.email?.takeIf { it.isNotBlank() && !job.user.guest } ?: return
        val names = job.files.mapNotNull { it.result?.originalName }
        runCatching { mail.sendConversionDone(email, job.id!!, names) }
            .onFailure { log.warn("Mail notify failed for job {}: {}", job.id, it.message) }
    }

    fun saveAndEmitProgress(job: ConversionJob) {
        jobs.save(job)
        emitProgress(job)
    }

    /**
     * Standard 1->1 loop used by audio/video/image/document(single) jobs: converts each input
     * with [converter], streaming progress, then moves the job to DONE (if any file succeeded)
     * or FAILED and emits the terminal event.
     */
    fun convertEachFile(job: ConversionJob, workDir: Path, converter: Converter) {
        val settings = job.toSettings()
        job.files.forEach { jobFile ->
            runCatching { convertOne(job, jobFile, converter, settings, workDir) }
                .onFailure { ex ->
                    log.warn("File {} failed: {}", jobFile.upload.id, ex.message)
                    jobFile.status = JobStatus.FAILED
                    jobFile.progress = 100
                    jobFile.error = ex.message?.take(500)
                }
            recomputeProgress(job)
            jobs.save(job)
            emitProgress(job)
        }

        val anyDone = job.files.any { it.status == JobStatus.DONE }
        job.status = if (anyDone) JobStatus.DONE else JobStatus.FAILED
        job.progress = 100
        jobs.save(job)
        emitDone(job)
    }

    private fun convertOne(
        job: ConversionJob,
        jobFile: JobFile,
        converter: Converter,
        settings: ConversionSettings,
        workDir: Path,
    ) {
        val upload = jobFile.upload

        jobFile.status = JobStatus.PROCESSING
        jobFile.progress = 15
        jobs.save(job)
        emitProgress(job)

        // Use the original name locally so the converter's output keeps a human name.
        val inputPath = workDir.resolve(upload.originalName)
        storage.download(upload.absolutePath, inputPath)

        val produced = converter.convert(inputPath, job.outputFormat, settings, workDir)
        val result = storeResult(job, produced)

        jobFile.name = result.originalName
        jobFile.result = result
        jobFile.bytes = result.bytes
        jobFile.status = JobStatus.DONE
        jobFile.progress = 100

        if (!job.keepOriginal) {
            storage.delete(upload.absolutePath)
            upload.active = false
            upload.deletedTimestamp = Instant.now().epochSecond
            runCatching { files.save(upload) }
        }
    }

    /**
     * Standard n->1 loop (merge-pdf/-audio/-videos, zip): downloads every input (index-prefixed
     * against name collisions), lets [combine] produce ONE output, attaches the result to the
     * first job file and marks the rest done — mirroring the merge-pdf flow.
     */
    fun combineAll(job: ConversionJob, workDir: Path, combine: (inputs: List<Path>) -> Path) {
        try {
            val inputs = job.files.mapIndexed { i, jobFile ->
                val upload = jobFile.upload
                val path = workDir.resolve("$i-${upload.originalName}")
                storage.download(upload.absolutePath, path)
                jobFile.status = JobStatus.PROCESSING
                jobFile.progress = 50
                path
            }
            saveAndEmitProgress(job)

            val produced = combine(inputs)
            val result = storeResult(job, produced)

            job.files.forEachIndexed { i, jobFile ->
                jobFile.status = JobStatus.DONE
                jobFile.progress = 100
                if (i == 0) {
                    jobFile.name = result.originalName
                    jobFile.result = result
                    jobFile.bytes = result.bytes
                }
            }
            job.status = JobStatus.DONE
            job.progress = 100
            jobs.save(job)
            emitDone(job)
        } catch (ex: Exception) {
            log.warn("Merge failed for job {}: {}", job.id, ex.message)
            failJob(job, ex.message?.take(500) ?: "Merge failed.")
        }
    }

    /** Uploads the produced file to storage and persists a RESULT [StoredFile]. */
    fun storeResult(
        job: ConversionJob,
        produced: Path,
        displayName: String = produced.fileName.toString(),
    ): StoredFile {
        val outName = displayName
        val bytes = Files.size(produced)
        val contentType = runCatching { Files.probeContentType(produced) }.getOrNull()
        val category = FileCategories.categoryOf(outName)
        val ext = FileCategories.extOf(outName)
        // The result is stored under its own UUID, foldered by format (image/, documents/, ...).
        val uuid = UUID.randomUUID()
        val key = Ids.absolutePath(category.folder, uuid.toString(), ext)
        storage.putFile(key, produced, contentType)

        return files.save(
            StoredFile(
                user = job.user,
                originalName = outName,
                name = Ids.objectName(uuid.toString(), ext),
                bytes = bytes,
                category = category,
                source = FileSource.RESULT,
                folder = category.folder,
                absolutePath = key,
                uuid = uuid,
                format = ext,
                contentType = contentType,
            ),
        )
    }

    fun recomputeProgress(job: ConversionJob) {
        job.progress = if (job.files.isEmpty()) 0 else job.files.sumOf { it.progress } / job.files.size
    }

    fun failJob(job: ConversionJob, message: String) {
        job.status = JobStatus.FAILED
        job.progress = 100
        job.files.forEach {
            it.status = JobStatus.FAILED
            it.progress = 100
            it.error = message
        }
        jobs.save(job)
        emitDone(job)
    }
}

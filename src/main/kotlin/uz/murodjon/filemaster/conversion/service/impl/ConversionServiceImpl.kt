package uz.murodjon.filemaster.conversion.service.impl

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.support.TransactionSynchronization
import org.springframework.transaction.support.TransactionSynchronizationManager
import uz.murodjon.filemaster.auth.model.User
import uz.murodjon.filemaster.conversion.dto.ConversionFilterRequest
import uz.murodjon.filemaster.conversion.dto.JobDto
import uz.murodjon.filemaster.conversion.model.ConversionJob
import uz.murodjon.filemaster.conversion.model.JobFile
import uz.murodjon.filemaster.conversion.repository.ConversionJobRepository
import uz.murodjon.filemaster.conversion.service.ConversionService
import uz.murodjon.filemaster.conversion.service.ConversionWorker
import uz.murodjon.filemaster.exception.JobNotFoundException
import uz.murodjon.filemaster.exception.NotFoundException
import uz.murodjon.filemaster.files.model.StoredFile
import uz.murodjon.filemaster.files.repository.StoredFileRepository
import uz.murodjon.filemaster.pageable.getPagination
import uz.murodjon.filemaster.storage.StorageService
import uz.murodjon.filemaster.tools.service.UserToolStatService
import uz.murodjon.filemaster.util.PageableData
import java.io.OutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

@Service
class ConversionServiceImpl(
    private val files: StoredFileRepository,
    private val jobs: ConversionJobRepository,
    private val storage: StorageService,
    private val worker: ConversionWorker,
    private val userToolStats: UserToolStatService,
) : ConversionService {

    private val log = LoggerFactory.getLogger(javaClass)

    @Transactional
    override fun submit(job: ConversionJob, sources: List<StoredFile>): JobDto {
        job.files = sources.map { source ->
            JobFile(job = job, upload = source, name = source.originalName)
        }.toMutableList()

        val saved = jobs.save(job)
        runCatching { userToolStats.recordUsage(job.user.id!!, job.tool.slug) }
            .onFailure { log.warn("Failed to record tool usage for {}: {}", job.tool.slug, it.message) }

        // Only start the worker once this transaction has committed, otherwise the
        // worker thread may query for the job before its row is visible.
        TransactionSynchronizationManager.registerSynchronization(object : TransactionSynchronization {
            override fun afterCommit() {
                worker.process(saved.id!!)
            }
        })
        return JobDto(saved)
    }

    @Transactional(readOnly = true)
    override fun status(user: User, jobId: Long): JobDto =
        JobDto(loadOwned(user, jobId))

    @Transactional(readOnly = true)
    override fun filter(user: User, filter: ConversionFilterRequest): PageableData<JobDto> {
        val page = getPagination(filter) { pageable ->
            jobs.findByUserIdAndStatusIn(user.id!!, filter.status, pageable)
        }
        return PageableData(
            totalPages = page.totalPages,
            currentPage = page.number + 1,
            totalElements = page.totalElements,
            data = page.content.map { JobDto(it) },
        )
    }

    @Transactional(readOnly = true)
    override fun writeResultsZip(user: User, jobId: Long, out: OutputStream) {
        val job = loadOwned(user, jobId)
        val results = job.files.mapNotNull { it.result }.filter { it.active }
        if (results.isEmpty()) throw NotFoundException("No results to download yet.")

        ZipOutputStream(out).use { zip ->
            val used = mutableSetOf<String>()
            results.forEach { rf ->
                zip.putNextEntry(ZipEntry(uniqueName(rf.originalName, used)))
                storage.get(rf.absolutePath).use { it.copyTo(zip) }
                zip.closeEntry()
            }
        }
    }

    private fun loadOwned(user: User, jobId: Long): ConversionJob =
        jobs.findByIdAndUserId(jobId, user.id!!).orElse(null) ?: throw JobNotFoundException()

    private fun uniqueName(name: String, used: MutableSet<String>): String {
        if (used.add(name)) return name
        val base = name.substringBeforeLast('.')
        val ext = name.substringAfterLast('.', "")
        var i = 1
        while (true) {
            val candidate = if (ext.isEmpty()) "$base ($i)" else "$base ($i).$ext"
            if (used.add(candidate)) return candidate
            i++
        }
    }
}

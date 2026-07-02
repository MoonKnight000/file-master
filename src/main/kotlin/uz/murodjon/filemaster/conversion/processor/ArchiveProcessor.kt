package uz.murodjon.filemaster.conversion.processor

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import uz.murodjon.filemaster.common.JobStatus
import uz.murodjon.filemaster.conversion.engine.ZipArchiver
import uz.murodjon.filemaster.conversion.model.ConversionJob
import uz.murodjon.filemaster.conversion.model.JobFile
import uz.murodjon.filemaster.tools.dto.ToolDef
import uz.murodjon.filemaster.tools.enums.ToolEngine
import uz.murodjon.filemaster.tools.enums.ToolGroup
import java.nio.file.Files
import java.nio.file.Path
import java.util.zip.ZipInputStream

/**
 * Archive jobs: `unzip-files` is the 1->n case (each entry becomes its own result file);
 * `zip-files` is the n->1 case (all inputs packed into one .zip).
 */
@Component
class ArchiveProcessor(
    private val support: ConversionSupport,
    private val zipArchiver: ZipArchiver,
) : ConversionProcessor {

    private val log = LoggerFactory.getLogger(javaClass)

    override val group = ToolGroup.ARCHIVE

    override fun process(job: ConversionJob, tool: ToolDef, workDir: Path) {
        when (tool.engine) {
            ToolEngine.ZIP_CREATE -> zipFiles(job, workDir)
            else -> unzip(job, workDir)
        }
    }

    /** Packs every input into one .zip (n->1), attaching the result to the first job file. */
    private fun zipFiles(job: ConversionJob, workDir: Path) {
        try {
            val names = mutableListOf<String>()
            val inputs = job.files.mapIndexed { i, jobFile ->
                val upload = jobFile.upload
                val path = workDir.resolve("$i-${upload.originalName}")
                support.storage.download(upload.absolutePath, path)
                names += upload.originalName
                jobFile.status = JobStatus.PROCESSING
                jobFile.progress = 50
                path
            }
            support.saveAndEmitProgress(job)

            val zipPath = workDir.resolve("archive.zip")
            zipArchiver.zip(inputs, names, zipPath)
            val result = support.storeResult(job, zipPath)

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
            support.jobs.save(job)
            support.emitDone(job)
        } catch (ex: Exception) {
            log.warn("Zip failed for job {}: {}", job.id, ex.message)
            support.failJob(job, ex.message?.take(500) ?: "Zip failed.")
        }
    }

    private fun unzip(job: ConversionJob, workDir: Path) {
        try {
            // Iterate a snapshot of the inputs; the extracted entries are appended afterwards.
            val extractedEntries = mutableListOf<JobFile>()
            job.files.toList().forEach { jobFile ->
                val upload = jobFile.upload
                val zipPath = workDir.resolve(upload.originalName)
                support.storage.download(upload.absolutePath, zipPath)
                jobFile.status = JobStatus.PROCESSING
                jobFile.progress = 50

                extractZip(zipPath, workDir.resolve("out-${jobFile.id}")).forEach { (temp, leaf) ->
                    val result = support.storeResult(job, temp, leaf)
                    extractedEntries += JobFile(
                        job = job,
                        upload = upload,
                        name = result.originalName,
                        status = JobStatus.DONE,
                        progress = 100,
                        result = result,
                        bytes = result.bytes,
                    )
                }
                jobFile.status = JobStatus.DONE
                jobFile.progress = 100
            }

            if (extractedEntries.isEmpty()) {
                support.failJob(job, "Archive is empty or contains no extractable files.")
                return
            }

            job.files.addAll(extractedEntries)
            job.status = JobStatus.DONE
            job.progress = 100
            support.jobs.save(job)
            support.emitDone(job)
        } catch (ex: Exception) {
            log.warn("Unzip failed for job {}: {}", job.id, ex.message)
            support.failJob(job, ex.message?.take(500) ?: "Unzip failed.")
        }
    }

    /**
     * Extracts a .zip to [dest], returning (tempFile, displayName) pairs. We write each entry
     * to our own flat, index-prefixed temp path (never `entry.name`), which side-steps Zip Slip.
     * Enforces [MAX_ENTRIES] and [MAX_DECOMPRESSED_BYTES] limits to guard against ZIP bombs.
     */
    private fun extractZip(zip: Path, dest: Path): List<Pair<Path, String>> {
        Files.createDirectories(dest)
        val results = mutableListOf<Pair<Path, String>>()
        var totalBytes = 0L
        ZipInputStream(Files.newInputStream(zip)).use { zis ->
            var entry = zis.nextEntry
            var index = 0
            while (entry != null) {
                if (index >= MAX_ENTRIES) throw IllegalStateException("Archive exceeds $MAX_ENTRIES entry limit.")
                if (!entry.isDirectory) {
                    val leaf = entry.name.substringAfterLast('/').substringAfterLast('\\').ifBlank { "file" }
                    val temp = dest.resolve("$index-$leaf")
                    Files.newOutputStream(temp).use { out ->
                        val buf = ByteArray(65536)
                        var n = zis.read(buf)
                        while (n >= 0) {
                            totalBytes += n
                            if (totalBytes > MAX_DECOMPRESSED_BYTES) {
                                throw IllegalStateException("Decompressed size exceeds ${MAX_DECOMPRESSED_BYTES / (1024 * 1024)} MB limit.")
                            }
                            out.write(buf, 0, n)
                            n = zis.read(buf)
                        }
                    }
                    results += temp to leaf
                    index++
                }
                zis.closeEntry()
                entry = zis.nextEntry
            }
        }
        return results
    }

    private companion object {
        const val MAX_ENTRIES = 10_000
        const val MAX_DECOMPRESSED_BYTES = 2L * 1024 * 1024 * 1024 // 2 GB
    }
}

package uz.murodjon.filemaster.conversion.processor

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import uz.murodjon.filemaster.common.JobStatus
import uz.murodjon.filemaster.conversion.engine.DjvuConverter
import uz.murodjon.filemaster.conversion.engine.GhostscriptConverter
import uz.murodjon.filemaster.conversion.engine.ImagesToPdfConverter
import uz.murodjon.filemaster.conversion.engine.LibreOfficeConverter
import uz.murodjon.filemaster.conversion.engine.PdfEditor
import uz.murodjon.filemaster.conversion.engine.PdfMerger
import uz.murodjon.filemaster.conversion.engine.PdfRasterizer
import uz.murodjon.filemaster.conversion.engine.PdfSecurity
import uz.murodjon.filemaster.conversion.engine.PdfStamper
import uz.murodjon.filemaster.conversion.engine.TesseractConverter
import uz.murodjon.filemaster.conversion.model.ConversionJob
import uz.murodjon.filemaster.document.model.DocumentConversionJob
import uz.murodjon.filemaster.conversion.model.JobFile
import uz.murodjon.filemaster.tools.dto.ToolDef
import uz.murodjon.filemaster.tools.enums.EditOperation
import uz.murodjon.filemaster.tools.enums.ToolEngine
import uz.murodjon.filemaster.tools.enums.ToolGroup
import java.nio.file.Path

/**
 * Document jobs (PDF / Word / Excel / Slides / OCR + the PDF edit/raster tools). Most are a
 * standard 1->1 conversion via LibreOffice, Ghostscript or Tesseract; merge-pdf and images-to-pdf
 * are n->1; pdf-to-images and split-pdf are 1->n.
 */
@Component
class DocumentProcessor(
    private val libreOffice: LibreOfficeConverter,
    private val ghostscript: GhostscriptConverter,
    private val tesseract: TesseractConverter,
    private val djvu: DjvuConverter,
    private val pdfMerger: PdfMerger,
    private val pdfRasterizer: PdfRasterizer,
    private val imagesToPdf: ImagesToPdfConverter,
    private val pdfEditor: PdfEditor,
    private val pdfStamper: PdfStamper,
    private val pdfSecurity: PdfSecurity,
    private val support: ConversionSupport,
) : ConversionProcessor {

    private val log = LoggerFactory.getLogger(javaClass)

    override val group = ToolGroup.DOCUMENT

    override fun process(job: ConversionJob, tool: ToolDef, workDir: Path) {
        when (tool.engine) {
            ToolEngine.PDF_MERGE -> mergePdf(job, workDir)
            ToolEngine.IMAGES_TO_PDF -> imagesToPdf(job, workDir)
            ToolEngine.PDF_RASTER -> explode(job, workDir) { input, _ ->
                pdfRasterizer.rasterize(input, job.outputFormat, job.toSettings().quality, workDir)
            }
            ToolEngine.PDF_EDIT -> pdfEdit(job, tool, workDir)
            ToolEngine.LIBREOFFICE -> support.convertEachFile(job, workDir, libreOffice)
            ToolEngine.GHOSTSCRIPT -> support.convertEachFile(job, workDir, ghostscript)
            ToolEngine.OCR -> support.convertEachFile(job, workDir, tesseract)
            ToolEngine.DJVU -> convertDjvu(job, workDir)
            else -> support.failJob(job, "This tool is not available yet.")
        }
    }

    /**
     * DjVu → PDF, with optional Ghostscript compression chained after.
     * Without compress it reuses the standard 1->1 loop; with compress it runs a two-step
     * pipeline per file so the intermediate PDF never hits storage.
     */
    private fun convertDjvu(job: ConversionJob, workDir: Path) {
        val settings = job.toSettings()
        if (!settings.compress) {
            support.convertEachFile(job, workDir, djvu)
            return
        }
        try {
            job.files.forEach { jobFile ->
                val upload = jobFile.upload
                jobFile.status = JobStatus.PROCESSING
                jobFile.progress = 15
                support.jobs.save(job)
                support.emitProgress(job)

                val inputPath = workDir.resolve(upload.originalName)
                support.storage.download(upload.absolutePath, inputPath)

                val pdfPath = djvu.convert(inputPath, "PDF", settings, workDir)
                val compressedPath = ghostscript.convert(pdfPath, "PDF", settings, workDir)
                val result = support.storeResult(job, compressedPath)

                jobFile.name = result.originalName
                jobFile.result = result
                jobFile.bytes = result.bytes
                jobFile.status = JobStatus.DONE
                jobFile.progress = 100

                if (!job.keepOriginal) {
                    support.storage.delete(upload.absolutePath)
                    upload.active = false
                    upload.deletedTimestamp = java.time.Instant.now().epochSecond
                    runCatching { support.files.save(upload) }
                }
                support.recomputeProgress(job)
                support.jobs.save(job)
                support.emitProgress(job)
            }
            val anyDone = job.files.any { it.status == JobStatus.DONE }
            job.status = if (anyDone) JobStatus.DONE else JobStatus.FAILED
            job.progress = 100
            support.jobs.save(job)
            support.emitDone(job)
        } catch (ex: Exception) {
            log.warn("DjVu convert failed for job {}: {}", job.id, ex.message)
            support.failJob(job, ex.message?.take(500) ?: "DjVu conversion failed.")
        }
    }

    /** split-pdf is 1->n; every other PDF edit is a 1->1 rewrite of each input. */
    private fun pdfEdit(job: ConversionJob, tool: ToolDef, workDir: Path) {
        val settings = job.toSettings()
        when (tool.editOperation) {
            EditOperation.SPLIT -> explode(job, workDir) { input, _ ->
                pdfEditor.split(input, settings.splitRanges, workDir)
            }
            EditOperation.ROTATE -> editEachFile(job, workDir, "rotated") { input, output ->
                pdfEditor.rotate(input, settings.rotateDegrees ?: 90, output)
            }
            EditOperation.DELETE_PAGES -> editEachFile(job, workDir, "edited") { input, output ->
                pdfEditor.deletePages(input, settings.pageRanges.orEmpty(), output)
            }
            EditOperation.EXTRACT_PAGES -> editEachFile(job, workDir, "extracted") { input, output ->
                pdfEditor.extractPages(input, settings.pageRanges.orEmpty(), output)
            }
            EditOperation.REORDER_PAGES -> editEachFile(job, workDir, "reordered") { input, output ->
                pdfEditor.reorderPages(input, settings.pageOrder.orEmpty(), output)
            }
            EditOperation.WATERMARK -> editEachFile(job, workDir, "watermarked") { input, output ->
                pdfStamper.watermark(input, settings.watermarkText.orEmpty(),
                    settings.watermarkPosition ?: "diagonal",
                    settings.watermarkOpacity ?: 0.3, settings.watermarkFontSize ?: 48, output)
            }
            EditOperation.PAGE_NUMBERS -> editEachFile(job, workDir, "numbered") { input, output ->
                pdfStamper.addPageNumbers(input, settings.pageNumberPosition ?: "bottom-center", output)
            }
            EditOperation.PROTECT -> editEachFile(job, workDir, "protected") { input, output ->
                pdfSecurity.protect(input, requireNotNull(settings.password) { "Password is required." }, output)
            }
            EditOperation.UNLOCK -> editEachFile(job, workDir, "unlocked") { input, output ->
                pdfSecurity.unlock(input, settings.password, output)
            }
            else -> support.failJob(job, "This PDF edit is not available yet.")
        }
        // The password has served its purpose — don't keep it at rest on the finished job row.
        if (job is DocumentConversionJob && job.password != null) {
            job.password = null
            support.jobs.save(job)
        }
    }

    /** Shared 1->1 PDF edit loop: download each input, run [edit], store the result. */
    private fun editEachFile(job: ConversionJob, workDir: Path, prefix: String, edit: (input: Path, output: Path) -> Unit) {
        try {
            job.files.forEach { jobFile ->
                val upload = jobFile.upload
                val input = workDir.resolve(upload.originalName)
                support.storage.download(upload.absolutePath, input)
                jobFile.status = JobStatus.PROCESSING
                jobFile.progress = 50

                val output = workDir.resolve("$prefix-${upload.originalName}")
                edit(input, output)
                val result = support.storeResult(job, output, upload.originalName)
                jobFile.name = result.originalName
                jobFile.result = result
                jobFile.bytes = result.bytes
                jobFile.status = JobStatus.DONE
                jobFile.progress = 100
            }
            job.status = JobStatus.DONE
            job.progress = 100
            support.jobs.save(job)
            support.emitDone(job)
        } catch (ex: Exception) {
            log.warn("PDF edit failed for job {}: {}", job.id, ex.message)
            support.failJob(job, ex.message?.take(500) ?: "PDF edit failed.")
        }
    }

    /**
     * Shared 1->n path: for each input, [produce] yields several result files; each becomes its own
     * appended [JobFile] (mirrors the unzip flow). The source entry is marked done with no result.
     */
    private fun explode(job: ConversionJob, workDir: Path, produce: (Path, JobFile) -> List<Path>) {
        try {
            val extra = mutableListOf<JobFile>()
            job.files.toList().forEach { jobFile ->
                val upload = jobFile.upload
                val input = workDir.resolve(upload.originalName)
                support.storage.download(upload.absolutePath, input)
                jobFile.status = JobStatus.PROCESSING
                jobFile.progress = 50

                produce(input, jobFile).forEach { produced ->
                    val result = support.storeResult(job, produced)
                    extra += JobFile(
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

            if (extra.isEmpty()) {
                support.failJob(job, "Nothing was produced.")
                return
            }
            job.files.addAll(extra)
            job.status = JobStatus.DONE
            job.progress = 100
            support.jobs.save(job)
            support.emitDone(job)
        } catch (ex: Exception) {
            log.warn("Document explode failed for job {}: {}", job.id, ex.message)
            support.failJob(job, ex.message?.take(500) ?: "Processing failed.")
        }
    }

    /** Combines all input images into one PDF (n->1), attaching the result to the first job file. */
    private fun imagesToPdf(job: ConversionJob, workDir: Path) {
        try {
            val inputs = job.files.mapIndexed { i, jobFile ->
                val upload = jobFile.upload
                val path = workDir.resolve("$i-${upload.originalName}")
                support.storage.download(upload.absolutePath, path)
                jobFile.status = JobStatus.PROCESSING
                jobFile.progress = 50
                path
            }
            support.saveAndEmitProgress(job)

            val pdfPath = workDir.resolve("combined.pdf")
            imagesToPdf.combine(inputs, pdfPath)
            val result = support.storeResult(job, pdfPath)

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
            log.warn("Images-to-PDF failed for job {}: {}", job.id, ex.message)
            support.failJob(job, ex.message?.take(500) ?: "Images to PDF failed.")
        }
    }

    /** Combines all inputs into one PDF (n->1), attaching the result to the first job file. */
    private fun mergePdf(job: ConversionJob, workDir: Path) {
        try {
            // Download all inputs (index-prefixed to avoid name collisions).
            val inputs = job.files.mapIndexed { i, jobFile ->
                val upload = jobFile.upload
                val path = workDir.resolve("$i-${upload.originalName}")
                support.storage.download(upload.absolutePath, path)
                jobFile.status = JobStatus.PROCESSING
                jobFile.progress = 50
                path
            }
            support.saveAndEmitProgress(job)

            val mergedPath = workDir.resolve("merged.pdf")
            pdfMerger.merge(inputs, mergedPath)
            val result = support.storeResult(job, mergedPath)

            // One merged output: attach it to the first entry, mark the rest done.
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
            log.warn("Merge failed for job {}: {}", job.id, ex.message)
            support.failJob(job, ex.message?.take(500) ?: "Merge failed.")
        }
    }
}

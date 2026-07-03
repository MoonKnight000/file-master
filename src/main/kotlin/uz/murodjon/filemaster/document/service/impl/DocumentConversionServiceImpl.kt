package uz.murodjon.filemaster.document.service.impl

import org.springframework.stereotype.Service
import uz.murodjon.filemaster.document.service.DocumentConversionService
import uz.murodjon.filemaster.auth.model.User
import uz.murodjon.filemaster.conversion.dto.ConversionOptions
import uz.murodjon.filemaster.document.dto.DocumentConvertRequest
import uz.murodjon.filemaster.conversion.dto.JobDto
import uz.murodjon.filemaster.document.model.DocumentConversionJob
import uz.murodjon.filemaster.conversion.engine.PdfSecurity
import uz.murodjon.filemaster.conversion.service.ConversionService
import uz.murodjon.filemaster.conversion.service.ConversionValidator
import uz.murodjon.filemaster.conversion.service.MediaValidator
import uz.murodjon.filemaster.common.Quality
import uz.murodjon.filemaster.exception.ValidationException
import uz.murodjon.filemaster.files.model.StoredFile
import uz.murodjon.filemaster.storage.StorageService
import uz.murodjon.filemaster.tools.enums.EditOperation
import uz.murodjon.filemaster.tools.enums.ToolGroup
import uz.murodjon.filemaster.tools.enums.ToolSlug
import java.nio.file.Files

@Service
class DocumentConversionServiceImpl(
    private val conversion: ConversionService,
    private val validator: ConversionValidator,
    private val mediaValidator: MediaValidator,
    private val pdfSecurity: PdfSecurity,
    private val storage: StorageService,
) : DocumentConversionService {

    override fun convert(user: User, request: DocumentConvertRequest): JobDto {
        val tool = validator.requireTool(request.tool, ToolGroup.DOCUMENT)
        val sources = validator.resolveInputs(user, request.fileIds, tool)

        val o = request.options
        mediaValidator.validatePdfEdit(
            ConversionOptions(
                rotateDegrees = o?.rotateDegrees,
                splitRanges = o?.splitRanges,
                pageRanges = o?.pageRanges,
                pageOrder = o?.pageOrder,
                watermarkText = o?.watermarkText,
                watermarkPosition = o?.watermarkPosition,
                watermarkOpacity = o?.watermarkOpacity,
                watermarkFontSize = o?.watermarkFontSize,
                pageNumberPosition = o?.pageNumberPosition,
                password = o?.password,
            ),
            tool.editOperation,
        )
        mediaValidator.validateOcr(ConversionOptions(ocrLanguage = o?.ocrLanguage))
        if (tool.editOperation == EditOperation.UNLOCK) verifyUnlockPassword(sources, o?.password)

        val format = validator.resolveOutputFormat(tool, o?.outputFormat)
        val job = DocumentConversionJob(
            user = user,
            tool = ToolSlug.from(tool.slug),
            outputFormat = format,
            quality = o?.quality ?: Quality.BALANCED,
            keepOriginal = o?.keepOriginal ?: tool.keepOriginalDefault,
            mergeIntoOne = o?.mergeIntoOne ?: tool.mergeIntoOneDefault,
            rotateDegrees = o?.rotateDegrees,
            splitRanges = o?.splitRanges,
            compress = o?.compress ?: false,
            ocrLanguage = o?.ocrLanguage,
            pageRanges = o?.pageRanges,
            pageOrder = o?.pageOrder,
            watermarkText = o?.watermarkText,
            watermarkPosition = o?.watermarkPosition,
            watermarkOpacity = o?.watermarkOpacity,
            watermarkFontSize = o?.watermarkFontSize,
            pageNumberPosition = o?.pageNumberPosition,
            password = o?.password,
        )
        return conversion.submit(job, sources)
    }

    /**
     * Fast-fails a wrong/missing unlock-pdf password at submit time (open + close via PDFBox)
     * instead of letting the user wait for the job to fail. Files above the probe cap skip the
     * check to keep submit snappy — the job-level error remains the fallback for them.
     */
    private fun verifyUnlockPassword(sources: List<StoredFile>, password: String?) {
        sources.filter { it.bytes <= MAX_PASSWORD_PROBE_BYTES }.forEach { source ->
            val tmp = Files.createTempFile("unlock-check-", ".pdf")
            try {
                storage.download(source.absolutePath, tmp)
                if (!pdfSecurity.passwordAccepted(tmp, password)) {
                    throw ValidationException(
                        "Wrong or missing password for this protected PDF.",
                        mapOf("fileId" to source.id),
                    )
                }
            } finally {
                Files.deleteIfExists(tmp)
            }
        }
    }

    private companion object {
        /** unlock-pdf submit-time password probe is skipped for PDFs larger than this. */
        const val MAX_PASSWORD_PROBE_BYTES = 25L * 1024 * 1024
    }
}
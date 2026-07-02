package uz.murodjon.filemaster.pdf.service

import org.springframework.stereotype.Service
import uz.murodjon.filemaster.auth.model.User
import uz.murodjon.filemaster.conversion.dto.ConversionOptions
import uz.murodjon.filemaster.pdf.dto.DocumentConvertRequest
import uz.murodjon.filemaster.conversion.dto.JobDto
import uz.murodjon.filemaster.pdf.model.DocumentConversionJob
import uz.murodjon.filemaster.conversion.service.ConversionService
import uz.murodjon.filemaster.conversion.service.ConversionValidator
import uz.murodjon.filemaster.conversion.service.MediaValidator
import uz.murodjon.filemaster.common.Quality
import uz.murodjon.filemaster.tools.enums.ToolGroup
import uz.murodjon.filemaster.tools.enums.ToolSlug

@Service
class DocumentConversionServiceImpl(
    private val conversion: ConversionService,
    private val validator: ConversionValidator,
    private val mediaValidator: MediaValidator,
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
}
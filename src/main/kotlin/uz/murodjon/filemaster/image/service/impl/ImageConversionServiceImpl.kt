package uz.murodjon.filemaster.image.service.impl

import org.springframework.stereotype.Service
import uz.murodjon.filemaster.auth.model.User
import uz.murodjon.filemaster.conversion.dto.ConversionOptions
import uz.murodjon.filemaster.image.dto.ImageConvertRequest
import uz.murodjon.filemaster.conversion.dto.JobDto
import uz.murodjon.filemaster.conversion.service.ConversionService
import uz.murodjon.filemaster.conversion.service.ConversionValidator
import uz.murodjon.filemaster.conversion.service.MediaValidator
import uz.murodjon.filemaster.image.model.ImageConversionJob
import uz.murodjon.filemaster.common.Quality
import uz.murodjon.filemaster.image.service.ImageConversionService
import uz.murodjon.filemaster.tools.enums.ToolGroup
import uz.murodjon.filemaster.tools.enums.ToolSlug

@Service
class ImageConversionServiceImpl(
    private val conversion: ConversionService,
    private val validator: ConversionValidator,
    private val mediaValidator: MediaValidator,
) : ImageConversionService {

    override fun convert(user: User, request: ImageConvertRequest): JobDto {
        val tool = validator.requireTool(request.tool, ToolGroup.IMAGE)
        val sources = validator.resolveInputs(user, request.fileIds, tool)

        val o = request.options
        mediaValidator.validateImage(
            ConversionOptions(
                imageWidth = o?.imageWidth,
                imageHeight = o?.imageHeight,
                imageQuality = o?.imageQuality,
                targetBytes = o?.targetBytes,
                rotateDegrees = o?.rotateDegrees,
            ),
        )

        val format = validator.resolveOutputFormat(tool, o?.outputFormat)
        val job = ImageConversionJob(
            user = user,
            tool = ToolSlug.from(tool.slug),
            outputFormat = format,
            quality = o?.quality ?: Quality.BALANCED,
            keepOriginal = o?.keepOriginal ?: tool.keepOriginalDefault,
            mergeIntoOne = tool.mergeIntoOneDefault,
            imageWidth = o?.imageWidth,
            imageHeight = o?.imageHeight,
            imageQuality = o?.imageQuality,
            targetBytes = o?.targetBytes,
            rotateDegrees = o?.rotateDegrees,
        )
        return conversion.submit(job, sources)
    }
}

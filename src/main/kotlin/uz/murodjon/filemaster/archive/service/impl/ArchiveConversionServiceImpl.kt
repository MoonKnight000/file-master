package uz.murodjon.filemaster.archive.service.impl

import org.springframework.stereotype.Service
import uz.murodjon.filemaster.archive.service.ArchiveConversionService
import uz.murodjon.filemaster.archive.dto.ArchiveConvertRequest
import uz.murodjon.filemaster.auth.model.User
import uz.murodjon.filemaster.conversion.dto.JobDto
import uz.murodjon.filemaster.conversion.model.ConversionJob
import uz.murodjon.filemaster.conversion.service.ConversionService
import uz.murodjon.filemaster.conversion.service.ConversionValidator
import uz.murodjon.filemaster.tools.enums.ToolGroup
import uz.murodjon.filemaster.tools.enums.ToolSlug

@Service
class ArchiveConversionServiceImpl(
    private val conversion: ConversionService,
    private val validator: ConversionValidator,
) : ArchiveConversionService {

    override fun convert(user: User, request: ArchiveConvertRequest): JobDto {
        val tool = validator.requireTool(request.tool, ToolGroup.ARCHIVE)
        val sources = validator.resolveInputs(user, request.fileIds, tool)

        val format = validator.resolveOutputFormat(tool, null)
        val job = ConversionJob(
            user = user,
            tool = ToolSlug.from(tool.slug),
            outputFormat = format,
            keepOriginal = request.options?.keepOriginal ?: tool.keepOriginalDefault,
        )
        return conversion.submit(job, sources)
    }
}
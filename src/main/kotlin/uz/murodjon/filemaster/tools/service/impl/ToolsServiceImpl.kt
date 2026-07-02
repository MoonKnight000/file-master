package uz.murodjon.filemaster.tools.service.impl

import org.springframework.stereotype.Service
import uz.murodjon.filemaster.common.CategoryToken
import uz.murodjon.filemaster.exception.ToolNotFoundException
import uz.murodjon.filemaster.tools.dto.AudioOptions
import uz.murodjon.filemaster.tools.dto.EditOptions
import uz.murodjon.filemaster.tools.dto.ImageOptions
import uz.murodjon.filemaster.tools.dto.ToolDetail
import uz.murodjon.filemaster.tools.dto.ToolListResponse
import uz.murodjon.filemaster.tools.dto.ToolOptions
import uz.murodjon.filemaster.tools.dto.VideoOptions
import uz.murodjon.filemaster.tools.enums.EditOperation
import uz.murodjon.filemaster.tools.enums.ToolKind
import uz.murodjon.filemaster.tools.service.ToolCatalog
import uz.murodjon.filemaster.tools.service.ToolProvider
import uz.murodjon.filemaster.tools.service.ToolsService

@Service
class ToolsServiceImpl(
    private val provider: ToolProvider,
    private val catalog: ToolCatalog,
) : ToolsService {

    override fun list(category: String?): ToolListResponse =
        ToolListResponse(provider.byCategory(category))

    override fun suggest(mime: String): ToolListResponse =
        ToolListResponse(provider.all().filter { tool -> tool.accept.any { it == mime } })

    override fun detail(slug: String): ToolDetail {
        val tool = provider.findBySlug(slug) ?: throw ToolNotFoundException(slug)
        return ToolDetail(
            slug = tool.slug,
            title = tool.title,
            desc = tool.desc,
            category = tool.category.token,
            kind = tool.kind.value,
            icon = tool.icon,
            badge = tool.badge?.value,
            accept = tool.accept,
            outputFormats = tool.outputFormats.map { it.value },
            defaultFormat = tool.defaultFormat.value,
            options = ToolOptions(
                quality = catalog.qualityOptions(),
                keepOriginal = tool.keepOriginalDefault,
                mergeIntoOne = tool.mergeIntoOneDefault,
                audio = if (tool.category == CategoryToken.AUDIO) {
                    AudioOptions(
                        bitratesKbps = catalog.audioBitrates(),
                        sampleRates = catalog.audioSampleRates(),
                        channels = catalog.audioChannels(),
                    )
                } else {
                    null
                },
                video = if (tool.category == CategoryToken.VIDEO) {
                    VideoOptions(
                        resolutions = catalog.videoResolutions(),
                        fps = catalog.videoFps(),
                        codecs = catalog.videoCodecs(),
                        maxBitrateKbps = catalog.maxVideoBitrateKbps(),
                        codecsByFormat = catalog.videoCodecsByFormat(),
                    )
                } else {
                    null
                },
                image = if (tool.category == CategoryToken.IMAGE) {
                    ImageOptions(
                        maxDimension = catalog.maxImageDimension(),
                        minQuality = catalog.minImageQuality(),
                        maxQuality = catalog.maxImageQuality(),
                    )
                } else {
                    null
                },
                edit = tool.editOperation?.let { op ->
                    EditOptions(
                        operation = op,
                        angles = if (op == EditOperation.ROTATE) listOf(90, 180, 270) else null,
                    )
                },
                ocrLanguages = if (tool.kind == ToolKind.OCR) catalog.ocrLanguages() else null,
            ),
        )
    }
}

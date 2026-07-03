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
import uz.murodjon.filemaster.tools.service.ToolSeo
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
            category = tool.category,
            kind = tool.kind,
            icon = tool.icon,
            badge = tool.badge,
            accept = tool.accept,
            outputFormats = tool.outputFormats,
            defaultFormat = tool.defaultFormat,
            longDescription = ToolSeo.longDescription(tool),
            faq = ToolSeo.faq(tool),
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
                    when (op) {
                        EditOperation.ROTATE -> EditOptions(op, angles = listOf(90, 180, 270))
                        EditOperation.WATERMARK -> EditOptions(
                            op,
                            positions = catalog.watermarkPositions(),
                            opacityMin = catalog.minWatermarkOpacity(),
                            opacityMax = catalog.maxWatermarkOpacity(),
                            fontSizeMin = catalog.minWatermarkFontSize(),
                            fontSizeMax = catalog.maxWatermarkFontSize(),
                            maxTextLength = catalog.maxWatermarkTextLength(),
                        )
                        EditOperation.PAGE_NUMBERS -> EditOptions(op, positions = catalog.pageNumberPositions())
                        EditOperation.FLIP -> EditOptions(op, flipDirections = catalog.flipDirections())
                        EditOperation.FILTER -> EditOptions(op, filters = catalog.imageFilters())
                        EditOperation.ADJUST -> EditOptions(op, adjustMin = catalog.minAdjust(), adjustMax = catalog.maxAdjust())
                        EditOperation.PROTECT -> EditOptions(
                            op,
                            passwordMinLength = catalog.minPasswordLength(),
                            passwordMaxLength = catalog.maxPasswordLength(),
                        )
                        EditOperation.SPEED -> EditOptions(
                            op,
                            speedMin = catalog.minSpeedFactor(),
                            speedMax = catalog.maxSpeedFactor(),
                        )
                        EditOperation.VOLUME -> EditOptions(
                            op,
                            volumeMin = catalog.minAudioVolume(),
                            volumeMax = catalog.maxAudioVolume(),
                        )
                        EditOperation.FADE -> EditOptions(op, fadeMaxSeconds = catalog.maxFadeSeconds())
                        // TRIM / SPLIT / RESIZE / DELETE_PAGES / EXTRACT_PAGES / REORDER_PAGES / UNLOCK /
                        // MUTE / REVERSE / NORMALIZE: the operation alone tells the front-end what to render.
                        else -> EditOptions(op)
                    }
                },
                ocrLanguages = if (tool.kind == ToolKind.OCR) catalog.ocrLanguages() else null,
            ),
        )
    }
}

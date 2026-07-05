package uz.murodjon.filemaster.conversion.service

import org.springframework.stereotype.Component
import uz.murodjon.filemaster.conversion.dto.ConversionOptions
import uz.murodjon.filemaster.exception.ValidationException
import uz.murodjon.filemaster.files.model.StoredFile
import uz.murodjon.filemaster.tools.enums.EditOperation
import uz.murodjon.filemaster.tools.service.ToolCatalog

/**
 * Per-category option validation, shared by the category conversion services
 * (e.g. video validates both video and audio-track knobs).
 */
@Component
class MediaValidator(private val catalog: ToolCatalog) {

    fun validateAudio(opts: ConversionOptions?, operation: EditOperation? = null) {
        requireEditFields(opts, operation)
        if (opts == null) return
        validateSpeedAndFade(opts)
        opts.audioBitrateKbps?.let {
            if (it !in catalog.audioBitrates()) {
                throw ValidationException("Unsupported bitrate: $it", mapOf("bitratesKbps" to catalog.audioBitrates()))
            }
        }
        opts.audioSampleRate?.let {
            if (it !in catalog.audioSampleRates()) {
                throw ValidationException("Unsupported sample rate: $it", mapOf("sampleRates" to catalog.audioSampleRates()))
            }
        }
        opts.audioChannels?.let {
            if (it !in catalog.audioChannels()) throw ValidationException("Channels must be 1 (mono) or 2 (stereo).")
        }
        opts.audioVolume?.let {
            if (it < catalog.minAudioVolume() || it > catalog.maxAudioVolume()) {
                throw ValidationException("Volume must be between ${catalog.minAudioVolume()} and ${catalog.maxAudioVolume()}.")
            }
        }
        opts.trimStartSeconds?.let { if (it < 0.0) throw ValidationException("trimStartSeconds must be >= 0.") }
        opts.trimEndSeconds?.let { end ->
            if (end <= 0.0) throw ValidationException("trimEndSeconds must be > 0.")
            if (end <= (opts.trimStartSeconds ?: 0.0)) {
                throw ValidationException("trimEndSeconds must be greater than trimStartSeconds.")
            }
        }
        opts.language?.let {
            if (it != "auto" && !it.matches(Regex("^[a-z]{2,3}$"))) {
                throw ValidationException("language must be an ISO-639 code (e.g. \"uz\", \"en\") or \"auto\".")
            }
        }
    }

    fun validateVideo(opts: ConversionOptions?, outputFormat: String, operation: EditOperation? = null) {
        requireEditFields(opts, operation)
        if (opts == null) return
        validateSpeedAndFade(opts)
        validateRotation(opts.rotateDegrees)
        validateCrop(opts)
        opts.flipDirection?.let {
            if (it !in catalog.flipDirections()) {
                throw ValidationException("Unsupported flip direction: $it", mapOf("directions" to catalog.flipDirections()))
            }
        }
        validateWatermark(opts)
        opts.videoResolution?.let {
            if (it !in catalog.videoResolutions()) {
                throw ValidationException("Unsupported resolution: $it", mapOf("resolutions" to catalog.videoResolutions()))
            }
        }
        opts.videoFps?.let {
            if (it !in catalog.videoFps()) throw ValidationException("Unsupported fps: $it", mapOf("fps" to catalog.videoFps()))
        }
        opts.videoCodec?.let {
            if (it !in catalog.videoCodecs()) {
                throw ValidationException("Unsupported codec: $it", mapOf("codecs" to catalog.videoCodecs()))
            }
            val compatible = catalog.videoCodecsFor(outputFormat)
            if (it !in compatible) {
                throw ValidationException("Codec '$it' is not compatible with $outputFormat.", mapOf("compatibleCodecs" to compatible))
            }
        }
        opts.videoBitrateKbps?.let {
            if (it < 100 || it > catalog.maxVideoBitrateKbps()) {
                throw ValidationException("Video bitrate must be 100..${catalog.maxVideoBitrateKbps()} kbps.")
            }
        }
    }

    fun validateImage(opts: ConversionOptions?, operation: EditOperation? = null) {
        requireEditFields(opts, operation)
        if (opts == null) return
        val maxDim = catalog.maxImageDimension()
        opts.imageWidth?.let { if (it < 1 || it > maxDim) throw ValidationException("imageWidth must be 1..$maxDim.") }
        opts.imageHeight?.let { if (it < 1 || it > maxDim) throw ValidationException("imageHeight must be 1..$maxDim.") }
        opts.imageQuality?.let {
            if (it < catalog.minImageQuality() || it > catalog.maxImageQuality()) {
                throw ValidationException("imageQuality must be ${catalog.minImageQuality()}..${catalog.maxImageQuality()}.")
            }
        }
        opts.targetBytes?.let {
            if (it < 1024) throw ValidationException("targetBytes must be at least 1024 (1 KB).")
        }
        validateRotation(opts.rotateDegrees)
        validateCrop(opts)
        opts.flipDirection?.let {
            if (it !in catalog.flipDirections()) {
                throw ValidationException("Unsupported flip direction: $it", mapOf("directions" to catalog.flipDirections()))
            }
        }
        opts.imageFilter?.let {
            if (it !in catalog.imageFilters()) {
                throw ValidationException("Unsupported filter: $it", mapOf("filters" to catalog.imageFilters()))
            }
        }
        listOf("brightness" to opts.brightness, "contrast" to opts.contrast, "saturation" to opts.saturation)
            .forEach { (name, value) ->
                value?.let {
                    if (it < catalog.minAdjust() || it > catalog.maxAdjust()) {
                        throw ValidationException("$name must be ${catalog.minAdjust()}..${catalog.maxAdjust()}.")
                    }
                }
            }
        validateWatermark(opts)
    }

    fun validateOcr(opts: ConversionOptions?) {
        if (opts == null) return
        opts.ocrLanguage?.let { lang ->
            val allowed = catalog.ocrLanguages()
            val parts = lang.split("+")
            val invalid = parts.filter { it !in allowed }
            if (invalid.isNotEmpty()) {
                throw ValidationException(
                    "Unsupported OCR language(s): ${invalid.joinToString()}",
                    mapOf("languages" to allowed),
                )
            }
        }
    }

    /** PDF edit knobs (rotate / split / delete / extract / reorder / watermark / page numbers / protect / unlock). */
    fun validatePdfEdit(opts: ConversionOptions?, operation: EditOperation? = null) {
        requireEditFields(opts, operation)
        if (opts == null) return
        validateRotation(opts.rotateDegrees)
        opts.splitRanges?.let {
            if (it.isBlank()) return@let // blank = one PDF per page (allowed)
            if (!SPLIT_RANGES.matches(it.trim())) {
                throw ValidationException("splitRanges must look like \"1-3,5,7-9\".")
            }
        }
        opts.pageRanges?.let {
            if (it.isNotBlank() && !SPLIT_RANGES.matches(it.trim())) {
                throw ValidationException("pageRanges must look like \"2,5-7\".")
            }
        }
        opts.pageOrder?.let {
            if (it.isNotBlank() && !PAGE_ORDER.matches(it.trim())) {
                throw ValidationException("pageOrder must be a comma-separated page list like \"3,1,2\".")
            }
        }
        opts.pageNumberPosition?.let {
            if (it !in catalog.pageNumberPositions()) {
                throw ValidationException("Unsupported position: $it", mapOf("positions" to catalog.pageNumberPositions()))
            }
        }
        opts.password?.let {
            // Never echo the value in the message — it flows to clients and logs.
            if (it.length < catalog.minPasswordLength() || it.length > catalog.maxPasswordLength()) {
                throw ValidationException("Password must be ${catalog.minPasswordLength()}..${catalog.maxPasswordLength()} characters.")
            }
        }
        validateWatermark(opts)
    }

    /** Watermark knobs shared by the PDF and image validators. */
    private fun validateWatermark(opts: ConversionOptions) {
        opts.watermarkText?.let {
            if (it.length > catalog.maxWatermarkTextLength()) {
                throw ValidationException("watermarkText must be at most ${catalog.maxWatermarkTextLength()} characters.")
            }
        }
        opts.watermarkPosition?.let {
            if (it !in catalog.watermarkPositions()) {
                throw ValidationException("Unsupported position: $it", mapOf("positions" to catalog.watermarkPositions()))
            }
        }
        opts.watermarkOpacity?.let {
            if (it < catalog.minWatermarkOpacity() || it > catalog.maxWatermarkOpacity()) {
                throw ValidationException("watermarkOpacity must be ${catalog.minWatermarkOpacity()}..${catalog.maxWatermarkOpacity()}.")
            }
        }
        opts.watermarkFontSize?.let {
            if (it < catalog.minWatermarkFontSize() || it > catalog.maxWatermarkFontSize()) {
                throw ValidationException("watermarkFontSize must be ${catalog.minWatermarkFontSize()}..${catalog.maxWatermarkFontSize()}.")
            }
        }
    }

    /** Speed / fade knobs shared by the audio and video validators. */
    private fun validateSpeedAndFade(opts: ConversionOptions) {
        opts.speedFactor?.let {
            if (it < catalog.minSpeedFactor() || it > catalog.maxSpeedFactor()) {
                throw ValidationException("speedFactor must be ${catalog.minSpeedFactor()}..${catalog.maxSpeedFactor()}.")
            }
        }
        listOf("fadeInSeconds" to opts.fadeInSeconds, "fadeOutSeconds" to opts.fadeOutSeconds)
            .forEach { (name, value) ->
                value?.let {
                    if (it < 0.0 || it > catalog.maxFadeSeconds()) {
                        throw ValidationException("$name must be 0..${catalog.maxFadeSeconds()}.")
                    }
                }
            }
    }

    /** Fields each edit operation cannot run without. */
    private fun requireEditFields(opts: ConversionOptions?, operation: EditOperation?) {
        when (operation) {
            EditOperation.DELETE_PAGES, EditOperation.EXTRACT_PAGES ->
                if (opts?.pageRanges.isNullOrBlank()) throw ValidationException("pageRanges is required (e.g. \"2,5-7\").")
            EditOperation.REORDER_PAGES ->
                if (opts?.pageOrder.isNullOrBlank()) throw ValidationException("pageOrder is required (e.g. \"3,1,2\").")
            EditOperation.WATERMARK ->
                if (opts?.watermarkText.isNullOrBlank()) throw ValidationException("watermarkText is required.")
            EditOperation.PROTECT ->
                if (opts?.password.isNullOrBlank()) throw ValidationException("password is required.")
            EditOperation.CROP ->
                if (opts?.cropWidth == null || opts.cropHeight == null || opts.cropX == null || opts.cropY == null) {
                    throw ValidationException("cropX, cropY, cropWidth and cropHeight are all required.")
                }
            EditOperation.FILTER ->
                if (opts?.imageFilter.isNullOrBlank()) {
                    throw ValidationException("filter is required.", mapOf("filters" to catalog.imageFilters()))
                }
            EditOperation.ADJUST ->
                if (opts?.brightness == null && opts?.contrast == null && opts?.saturation == null) {
                    throw ValidationException("At least one of brightness, contrast or saturation is required.")
                }
            EditOperation.SPEED ->
                if (opts?.speedFactor == null) {
                    throw ValidationException("speedFactor is required (${catalog.minSpeedFactor()}..${catalog.maxSpeedFactor()}).")
                }
            EditOperation.VOLUME ->
                if (opts?.audioVolume == null) {
                    throw ValidationException("audioVolume is required (${catalog.minAudioVolume()}..${catalog.maxAudioVolume()}).")
                }
            EditOperation.FADE ->
                if (opts?.fadeInSeconds == null && opts?.fadeOutSeconds == null) {
                    throw ValidationException("At least one of fadeInSeconds or fadeOutSeconds is required.")
                }
            // TRIM has safe defaults; UNLOCK's password is optional; MUTE/REVERSE/NORMALIZE need no knobs.
            else -> Unit
        }
    }

    /**
     * Submit-time crop bounds check against each source's probed pixel dimensions, so an
     * out-of-bounds crop fails the request instead of the job minutes later. Sources with
     * unknown dimensions (old rows, unprobeable files) are skipped — the job-level failure
     * stays as the fallback for those.
     */
    fun validateCropBounds(opts: ConversionOptions?, sources: List<StoredFile>) {
        val x = opts?.cropX ?: return
        val y = opts.cropY ?: return
        val w = opts.cropWidth ?: return
        val h = opts.cropHeight ?: return
        sources.forEach { source ->
            val width = source.width ?: return@forEach
            val height = source.height ?: return@forEach
            if (x + w > width || y + h > height) {
                throw ValidationException(
                    "Crop area (${w}x$h at $x,$y) exceeds '${source.originalName}' (${width}x$height).",
                    mapOf("fileId" to source.id, "originalWidth" to width, "originalHeight" to height),
                )
            }
        }
    }

    private fun validateCrop(opts: ConversionOptions) {
        val fields = listOf(opts.cropX, opts.cropY, opts.cropWidth, opts.cropHeight)
        if (fields.all { it == null }) return
        if (fields.any { it == null }) {
            throw ValidationException("cropX, cropY, cropWidth and cropHeight must be provided together.")
        }
        val maxDim = catalog.maxImageDimension()
        if (opts.cropX!! < 0 || opts.cropY!! < 0) throw ValidationException("cropX and cropY must be >= 0.")
        if (opts.cropWidth!! < 1 || opts.cropWidth > maxDim || opts.cropHeight!! < 1 || opts.cropHeight > maxDim) {
            throw ValidationException("cropWidth and cropHeight must be 1..$maxDim.")
        }
    }

    private fun validateRotation(degrees: Int?) {
        degrees?.let {
            if (it !in ALLOWED_ANGLES) {
                throw ValidationException("rotateDegrees must be one of $ALLOWED_ANGLES.")
            }
        }
    }

    private companion object {
        val ALLOWED_ANGLES = listOf(90, 180, 270)
        val SPLIT_RANGES = Regex("""\d+(-\d+)?(,\s*\d+(-\d+)?)*""")
        val PAGE_ORDER = Regex("""\d+(\s*,\s*\d+)*""")
    }
}

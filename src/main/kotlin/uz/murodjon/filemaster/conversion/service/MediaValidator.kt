package uz.murodjon.filemaster.conversion.service

import org.springframework.stereotype.Component
import uz.murodjon.filemaster.conversion.dto.ConversionOptions
import uz.murodjon.filemaster.exception.ValidationException
import uz.murodjon.filemaster.tools.service.ToolCatalog

/**
 * Per-category option validation, shared by the category conversion services
 * (e.g. video validates both video and audio-track knobs).
 */
@Component
class MediaValidator(private val catalog: ToolCatalog) {

    fun validateAudio(opts: ConversionOptions?) {
        if (opts == null) return
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
            if (it < 0.0 || it > 4.0) throw ValidationException("Volume must be between 0.0 and 4.0.")
        }
        opts.trimStartSeconds?.let { if (it < 0.0) throw ValidationException("trimStartSeconds must be >= 0.") }
        opts.trimEndSeconds?.let { end ->
            if (end <= 0.0) throw ValidationException("trimEndSeconds must be > 0.")
            if (end <= (opts.trimStartSeconds ?: 0.0)) {
                throw ValidationException("trimEndSeconds must be greater than trimStartSeconds.")
            }
        }
    }

    fun validateVideo(opts: ConversionOptions?, outputFormat: String) {
        if (opts == null) return
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

    fun validateImage(opts: ConversionOptions?) {
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

    /** PDF edit knobs (rotate-pdf / split-pdf). */
    fun validatePdfEdit(opts: ConversionOptions?) {
        if (opts == null) return
        validateRotation(opts.rotateDegrees)
        opts.splitRanges?.let {
            if (it.isBlank()) return@let // blank = one PDF per page (allowed)
            if (!SPLIT_RANGES.matches(it.trim())) {
                throw ValidationException("splitRanges must look like \"1-3,5,7-9\".")
            }
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
    }
}

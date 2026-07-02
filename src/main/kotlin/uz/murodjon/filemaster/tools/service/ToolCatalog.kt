package uz.murodjon.filemaster.tools.service

import org.springframework.stereotype.Component

/**
 * Global option vocabularies shared across tools (quality presets, the allowed audio/video/image
 * knobs). These are not per-tool data, so they stay in code; the per-tool catalog itself lives in
 * the `tools` table and is read via [ToolProvider].
 */
@Component
class ToolCatalog {

    private val quality = listOf("high", "balanced", "small")
    private val audioBitrates = listOf(64, 96, 128, 160, 192, 256, 320)
    private val audioSampleRates = listOf(8000, 16000, 22050, 32000, 44100, 48000)
    private val audioChannels = listOf(1, 2)
    private val videoResolutions = listOf("2160p", "1440p", "1080p", "720p", "480p", "360p")
    private val videoFps = listOf(24, 25, 30, 48, 50, 60)
    private val videoCodecs = listOf("h264", "h265", "vp9")
    private val maxVideoBitrateKbps = 50_000

    private val maxImageDimension = 10_000
    private val minImageQuality = 1
    private val maxImageQuality = 100
    // Tesseract language codes. Add more as traineddata files are installed.
    private val ocrLanguages = listOf("eng", "rus", "uzb", "uzb_cyrl")

    // Edit-tool vocabularies (watermark / page numbers / flip / filters).
    private val watermarkPositions = listOf("top-left", "top-right", "center", "bottom-left", "bottom-right", "diagonal")
    private val pageNumberPositions = listOf("top-left", "top-center", "top-right", "bottom-left", "bottom-center", "bottom-right")
    private val flipDirections = listOf("horizontal", "vertical")
    private val imageFilters = listOf("grayscale", "sepia", "invert", "blur", "sharpen")
    private val minWatermarkOpacity = 0.05
    private val maxWatermarkOpacity = 1.0
    private val minWatermarkFontSize = 6
    private val maxWatermarkFontSize = 144
    private val maxWatermarkTextLength = 100
    private val minAdjust = -100
    private val maxAdjust = 100
    private val minPasswordLength = 4
    private val maxPasswordLength = 128
    private val minSpeedFactor = 0.25
    private val maxSpeedFactor = 4.0
    private val minAudioVolume = 0.0
    private val maxAudioVolume = 4.0
    private val maxFadeSeconds = 300.0

    /** Which video codecs each output container (format) accepts. Keys are UPPERCASE formats. */
    private val videoCodecsByFormat = mapOf(
        "MP4" to listOf("h264", "h265"),
        "MOV" to listOf("h264", "h265"),
        "MKV" to listOf("h264", "h265", "vp9"),
        "WEBM" to listOf("vp9"),
        "AVI" to listOf("h264"),
    )

    fun qualityOptions(): List<String> = quality

    fun audioBitrates(): List<Int> = audioBitrates
    fun audioSampleRates(): List<Int> = audioSampleRates
    fun audioChannels(): List<Int> = audioChannels

    fun videoResolutions(): List<String> = videoResolutions
    fun videoFps(): List<Int> = videoFps
    fun videoCodecs(): List<String> = videoCodecs
    fun maxVideoBitrateKbps(): Int = maxVideoBitrateKbps
    fun videoCodecsByFormat(): Map<String, List<String>> = videoCodecsByFormat

    /** Codecs compatible with the given output container/format (empty if unknown). */
    fun videoCodecsFor(format: String): List<String> = videoCodecsByFormat[format.uppercase()] ?: emptyList()

    fun maxImageDimension(): Int = maxImageDimension
    fun minImageQuality(): Int = minImageQuality
    fun maxImageQuality(): Int = maxImageQuality
    fun ocrLanguages(): List<String> = ocrLanguages

    fun watermarkPositions(): List<String> = watermarkPositions
    fun pageNumberPositions(): List<String> = pageNumberPositions
    fun flipDirections(): List<String> = flipDirections
    fun imageFilters(): List<String> = imageFilters
    fun minWatermarkOpacity(): Double = minWatermarkOpacity
    fun maxWatermarkOpacity(): Double = maxWatermarkOpacity
    fun minWatermarkFontSize(): Int = minWatermarkFontSize
    fun maxWatermarkFontSize(): Int = maxWatermarkFontSize
    fun maxWatermarkTextLength(): Int = maxWatermarkTextLength
    fun minAdjust(): Int = minAdjust
    fun maxAdjust(): Int = maxAdjust
    fun minPasswordLength(): Int = minPasswordLength
    fun maxPasswordLength(): Int = maxPasswordLength
    fun minSpeedFactor(): Double = minSpeedFactor
    fun maxSpeedFactor(): Double = maxSpeedFactor
    fun minAudioVolume(): Double = minAudioVolume
    fun maxAudioVolume(): Double = maxAudioVolume
    fun maxFadeSeconds(): Double = maxFadeSeconds
}

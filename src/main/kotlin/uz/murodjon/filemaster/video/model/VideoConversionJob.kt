package uz.murodjon.filemaster.video.model

import jakarta.persistence.Entity
import jakarta.persistence.Table
import uz.murodjon.filemaster.auth.model.User
import uz.murodjon.filemaster.common.Quality
import uz.murodjon.filemaster.conversion.engine.ConversionSettings
import uz.murodjon.filemaster.conversion.model.ConversionJob
import uz.murodjon.filemaster.tools.enums.ToolSlug

/**
 * Video conversion job: video knobs plus the knobs applied to the video's audio track. These
 * audio-track columns are this table's own — distinct from [uz.murodjon.filemaster.audio.model.AudioConversionJob]'s.
 */
@Entity
@Table(name = "video_conversion_jobs")
class VideoConversionJob(
    user: User,
    tool: ToolSlug,
    outputFormat: String,
    quality: Quality = Quality.BALANCED,
    keepOriginal: Boolean = true,
    mergeIntoOne: Boolean = false,
    // Video knobs (null = engine default).
    var videoResolution: String? = null,
    var videoFps: Int? = null,
    var videoBitrateKbps: Int? = null,
    var videoCodec: String? = null,
    var muteAudio: Boolean = false,
    // Applied to the video's audio track (ignored when muteAudio = true).
    var audioBitrateKbps: Int? = null,
    var audioSampleRate: Int? = null,
    var audioChannels: Int? = null,
    var audioVolume: Double? = null,
    var audioNormalize: Boolean = false,
    var trimStartSeconds: Double? = null,
    var trimEndSeconds: Double? = null,
    var speedFactor: Double? = null,
    var rotateDegrees: Int? = null,
    var flipDirection: String? = null,
    var cropX: Int? = null,
    var cropY: Int? = null,
    var cropWidth: Int? = null,
    var cropHeight: Int? = null,
    var watermarkText: String? = null,
    var watermarkPosition: String? = null,
    var watermarkOpacity: Double? = null,
    var watermarkFontSize: Int? = null,
) : ConversionJob(user, tool, outputFormat, quality, keepOriginal, mergeIntoOne) {

    override fun toSettings(): ConversionSettings = super.toSettings().copy(
        videoResolution = videoResolution,
        videoFps = videoFps,
        videoBitrateKbps = videoBitrateKbps,
        videoCodec = videoCodec,
        muteAudio = muteAudio,
        audioBitrateKbps = audioBitrateKbps,
        audioSampleRate = audioSampleRate,
        audioChannels = audioChannels,
        audioVolume = audioVolume,
        audioNormalize = audioNormalize,
        trimStartSeconds = trimStartSeconds,
        trimEndSeconds = trimEndSeconds,
        speedFactor = speedFactor,
        rotateDegrees = rotateDegrees,
        flipDirection = flipDirection,
        cropX = cropX,
        cropY = cropY,
        cropWidth = cropWidth,
        cropHeight = cropHeight,
        watermarkText = watermarkText,
        watermarkPosition = watermarkPosition,
        watermarkOpacity = watermarkOpacity,
        watermarkFontSize = watermarkFontSize,
    )
}

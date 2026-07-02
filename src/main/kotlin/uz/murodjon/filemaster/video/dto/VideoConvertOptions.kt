package uz.murodjon.filemaster.video.dto

import uz.murodjon.filemaster.common.Quality

data class VideoConvertOptions(
    val outputFormat: String? = null,
    val quality: Quality? = null,
    val keepOriginal: Boolean? = null,
    val videoResolution: String? = null,
    val videoFps: Int? = null,
    val videoBitrateKbps: Int? = null,
    val videoCodec: String? = null,
    val muteAudio: Boolean? = null,
    // Apply to the video's audio track (ignored when muteAudio = true).
    val audioBitrateKbps: Int? = null,
    val audioSampleRate: Int? = null,
    val audioChannels: Int? = null,
    val audioVolume: Double? = null,
    val audioNormalize: Boolean? = null,
    val trimStartSeconds: Double? = null,
    val trimEndSeconds: Double? = null,
    val speedFactor: Double? = null,    // speed-video: 0.25..4 (video + audio together)
    val rotateDegrees: Int? = null,     // rotate-video: 90 | 180 | 270
    val flipDirection: String? = null,  // flip-video: "horizontal" | "vertical"
    val cropX: Int? = null,             // crop-video: rectangle in source-frame pixels
    val cropY: Int? = null,
    val cropWidth: Int? = null,
    val cropHeight: Int? = null,
    val watermarkText: String? = null,  // watermark-video
    val watermarkPosition: String? = null,
    val watermarkOpacity: Double? = null,
    val watermarkFontSize: Int? = null,
)

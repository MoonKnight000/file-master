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
)

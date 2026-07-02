package uz.murodjon.filemaster.audio.dto

import uz.murodjon.filemaster.common.Quality

data class AudioConvertOptions(
    val outputFormat: String? = null,
    val quality: Quality? = null,
    val keepOriginal: Boolean? = null,
    val audioBitrateKbps: Int? = null,
    val audioSampleRate: Int? = null,
    val audioChannels: Int? = null,
    val audioVolume: Double? = null,
    val audioNormalize: Boolean? = null,
    val trimStartSeconds: Double? = null,
    val trimEndSeconds: Double? = null,
    val speedFactor: Double? = null,    // speed-audio: 0.25..4
    val fadeInSeconds: Double? = null,  // fade-audio
    val fadeOutSeconds: Double? = null, // fade-audio
)

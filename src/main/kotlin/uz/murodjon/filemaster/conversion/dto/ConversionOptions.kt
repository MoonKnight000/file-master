package uz.murodjon.filemaster.conversion.dto

import uz.murodjon.filemaster.common.Quality

data class ConversionOptions(
    val outputFormat: String? = null,
    val quality: Quality? = null,
    val keepOriginal: Boolean? = null,
    val mergeIntoOne: Boolean? = null,
    // Audio-specific (used by the Convert Audio tool; all optional).
    val audioBitrateKbps: Int? = null,
    val audioSampleRate: Int? = null,
    val audioChannels: Int? = null,   // 1 = mono, 2 = stereo
    val audioVolume: Double? = null,  // gain multiplier, 1.0 = unchanged
    val trimStartSeconds: Double? = null,
    val trimEndSeconds: Double? = null,
    // Video-specific (used by the Convert Video tool; all optional).
    val videoResolution: String? = null, // "2160p" | "1440p" | "1080p" | "720p" | "480p" | "360p"
    val videoFps: Int? = null,
    val videoBitrateKbps: Int? = null,
    val videoCodec: String? = null,      // "h264" | "h265" | "vp9"
    // Image-specific (used by the Compress/Convert Image tools; all optional).
    val imageWidth: Int? = null,
    val imageHeight: Int? = null,
    val imageQuality: Int? = null,       // 1..100
    // Edit-specific (rotate-image/rotate-pdf, split-pdf). trim* above is reused by trim-audio/video.
    val rotateDegrees: Int? = null,      // 90 | 180 | 270
    val splitRanges: String? = null,     // e.g. "1-3,5,7-9"; blank/null = one PDF per page
    // Audio normalize (loudnorm).
    val audioNormalize: Boolean? = null,
    // Video mute (strip audio track).
    val muteAudio: Boolean? = null,
    // Image target size in bytes (compress-image; JPEG only).
    val targetBytes: Long? = null,
    // OCR language code(s) e.g. "eng", "rus", "uzb".
    val ocrLanguage: String? = null,
    // Transcription language for audio-to-text: ISO-639 code ("uz", "en", ...) or "auto".
    val language: String? = null,
    // PDF page edits (delete/extract: flat "2,5-7" selection, ONE output; reorder: "3,1,2").
    val pageRanges: String? = null,
    val pageOrder: String? = null,
    // Stamp overlays (watermark-pdf/-image, page-numbers-pdf).
    val watermarkText: String? = null,
    val watermarkPosition: String? = null,
    val watermarkOpacity: Double? = null,
    val watermarkFontSize: Int? = null,
    val pageNumberPosition: String? = null,
    // PDF security (protect/unlock). Never echo in validation messages or logs.
    val password: String? = null,
    // Image edits.
    val cropX: Int? = null,
    val cropY: Int? = null,
    val cropWidth: Int? = null,
    val cropHeight: Int? = null,
    val flipDirection: String? = null,
    val imageFilter: String? = null,
    val brightness: Int? = null,
    val contrast: Int? = null,
    val saturation: Int? = null,
    // Audio/video edits.
    val speedFactor: Double? = null,
    val fadeInSeconds: Double? = null,
    val fadeOutSeconds: Double? = null,
)

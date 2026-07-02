package uz.murodjon.filemaster.conversion.engine

import uz.murodjon.filemaster.common.Quality

/**
 * Everything a [Converter] may need beyond the input/output format. [quality] applies to
 * every engine; the rest are audio/media knobs only ffmpeg uses (null = leave to the
 * engine default / derive from quality).
 */
data class ConversionSettings(
    val quality: Quality,
    val audioBitrateKbps: Int? = null,
    val audioSampleRate: Int? = null,
    val audioChannels: Int? = null,
    val audioVolume: Double? = null,
    val trimStartSeconds: Double? = null,
    val trimEndSeconds: Double? = null,
    // Video knobs (ffmpeg, video targets only).
    val videoResolution: String? = null, // e.g. "1080p", "720p" — scales by height, keeps aspect
    val videoFps: Int? = null,
    val videoBitrateKbps: Int? = null,
    val videoCodec: String? = null,      // "h264" | "h265" | "vp9"
    // Image knobs (image targets only).
    val imageWidth: Int? = null,         // resize: fits within the given bound(s), keeps aspect
    val imageHeight: Int? = null,
    val imageQuality: Int? = null,       // 1..100 (JPEG/WebP); null = derive from quality
    // Edit knobs.
    val rotateDegrees: Int? = null,      // 90 | 180 | 270 (image / pdf rotate)
    val splitRanges: String? = null,     // split-pdf: "1-3,5"; blank/null = one PDF per page
    // Post-conversion compression (djvu-to-pdf): run Ghostscript after the primary conversion.
    val compress: Boolean = false,
    // Audio normalization: apply ffmpeg loudnorm filter after any volume adjustment.
    val audioNormalize: Boolean = false,
    // Video: strip the audio track entirely (-an).
    val muteAudio: Boolean = false,
    // Image compress-to-size: target output file size in bytes (JPEG only; 0/null = disabled).
    val targetBytes: Long? = null,
    // OCR: Tesseract language code(s), e.g. "eng", "rus+uzb". null = tesseract default (eng).
    val ocrLanguage: String? = null,
)

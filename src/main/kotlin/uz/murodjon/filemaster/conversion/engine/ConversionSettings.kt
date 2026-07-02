package uz.murodjon.filemaster.conversion.engine

import uz.murodjon.filemaster.common.Quality

/**
 * Everything a [Converter] may need beyond the input/output format. [quality] applies to
 * every engine; the rest are audio/media knobs only ffmpeg uses (null = leave to the
 * engine default / derive from quality).
 *
 * SECURITY: [password] is part of this data class — never log a whole settings (or job)
 * instance, its `toString()` would leak the password.
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
    // PDF page edits. pageRanges is a flat selection ("2,5-7") producing ONE output —
    // unlike splitRanges, which groups pages into MULTIPLE output PDFs.
    val pageRanges: String? = null,      // delete-pdf-pages / extract-pdf-pages
    val pageOrder: String? = null,       // reorder-pdf-pages: "3,1,2" — full permutation of 1..N
    // Stamp overlays (watermark-pdf / watermark-image / page-numbers-pdf).
    val watermarkText: String? = null,
    val watermarkPosition: String? = null, // catalog vocabulary; "diagonal" = 45° across the center
    val watermarkOpacity: Double? = null,  // 0.05..1.0
    val watermarkFontSize: Int? = null,    // 6..144
    val pageNumberPosition: String? = null,
    // PDF security (protect: the password to set; unlock: the current open password). NEVER log.
    val password: String? = null,
    // Image edits.
    val cropX: Int? = null,
    val cropY: Int? = null,
    val cropWidth: Int? = null,
    val cropHeight: Int? = null,
    val flipDirection: String? = null,   // "horizontal" | "vertical"
    val imageFilter: String? = null,     // grayscale | sepia | invert | blur | sharpen
    val brightness: Int? = null,         // -100..100
    val contrast: Int? = null,           // -100..100
    val saturation: Int? = null,         // -100..100
    // Audio/video edits (ffmpeg).
    val speedFactor: Double? = null,     // playback rate 0.25..4 (setpts + atempo)
    val fadeInSeconds: Double? = null,   // afade t=in
    val fadeOutSeconds: Double? = null,  // afade t=out (needs the input duration — ffprobe)
    val reverseAudio: Boolean = false,   // areverse
)

package uz.murodjon.filemaster.pdf.dto

import uz.murodjon.filemaster.common.Quality

data class DocumentConvertOptions(
    val outputFormat: String? = null,
    val quality: Quality? = null,
    val keepOriginal: Boolean? = null,
    val mergeIntoOne: Boolean? = null,   // merge-pdf
    val rotateDegrees: Int? = null,      // rotate-pdf: 90 | 180 | 270
    val splitRanges: String? = null,     // split-pdf: "1-3,5"; blank = one PDF per page
    val compress: Boolean? = null,       // djvu-to-pdf: also compress the resulting PDF
    val ocrLanguage: String? = null,     // ocr-scan: Tesseract language code(s) e.g. "eng", "rus", "uzb"
)

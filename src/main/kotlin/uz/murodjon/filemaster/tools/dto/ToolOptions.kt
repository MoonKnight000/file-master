package uz.murodjon.filemaster.tools.dto

data class ToolOptions(
    val quality: List<String>,
    val keepOriginal: Boolean,
    val mergeIntoOne: Boolean,
    /** Present only for audio tools — the detailed audio knobs to render. */
    val audio: AudioOptions? = null,
    /** Present only for video tools — the detailed video knobs to render. */
    val video: VideoOptions? = null,
    /** Present only for image tools — the detailed image knobs to render. */
    val image: ImageOptions? = null,
    /** Present only for `kind == EDIT` tools — which interactive edit form to render. */
    val edit: EditOptions? = null,
    /** Present only for `kind == OCR` tools — Tesseract language codes the user may select. */
    val ocrLanguages: List<String>? = null,
)

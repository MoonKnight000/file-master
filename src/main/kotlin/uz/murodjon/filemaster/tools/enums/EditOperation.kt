package uz.murodjon.filemaster.tools.enums

import com.fasterxml.jackson.annotation.JsonValue

/**
 * The concrete edit a `kind == EDIT` tool performs — tells the front-end which interactive
 * form to render (rotate dial, trim slider, page-range picker, resize box). Null for every
 * non-edit tool.
 */
enum class EditOperation(@get:JsonValue val value: String) {
    ROTATE("rotate"),
    TRIM("trim"),
    SPLIT("split"),
    RESIZE("resize"),
    DELETE_PAGES("delete-pages"),
    EXTRACT_PAGES("extract-pages"),
    REORDER_PAGES("reorder-pages"),
    WATERMARK("watermark"),      // shared by watermark-pdf and watermark-image (same form)
    PAGE_NUMBERS("page-numbers"),
    PROTECT("protect"),
    UNLOCK("unlock"),
    CROP("crop"),
    FLIP("flip"),
    FILTER("filter"),
    ADJUST("adjust"),
    SPEED("speed"),          // speed-video / speed-audio: playback rate 0.25x..4x
    MUTE("mute"),            // mute-video: strip the audio track (no knobs)
    VOLUME("volume"),        // volume-audio: gain 0..4
    FADE("fade"),            // fade-audio: fade-in / fade-out seconds
    REVERSE("reverse"),      // reverse-audio (no knobs)
    NORMALIZE("normalize"),  // normalize-audio: loudnorm (no knobs)
}

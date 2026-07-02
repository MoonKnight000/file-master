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
}

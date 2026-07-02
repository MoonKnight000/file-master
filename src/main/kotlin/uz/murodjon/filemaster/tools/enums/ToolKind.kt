package uz.murodjon.filemaster.tools.enums

import com.fasterxml.jackson.annotation.JsonValue

/**
 * The UX shape of a tool — lets the front-end render the right convert form
 * (e.g. format picker for `convert`, just quality for `compress`, multi-file for
 * `merge`, plain run for `unzip`/`ocr`).
 */
enum class ToolKind(@get:JsonValue val value: String) {
    CONVERT("convert"),
    COMPRESS("compress"),
    MERGE("merge"),
    UNZIP("unzip"),
    OCR("ocr"),
    EDIT("edit"),
}

package uz.murodjon.filemaster.files.enums

import com.fasterxml.jackson.annotation.JsonValue

/** Where a [uz.murodjon.filemaster.files.model.StoredFile] came from. */
enum class FileSource(@get:JsonValue val value: String) {
    /** A source file the user uploaded (conversion input). */
    UPLOAD("upload"),

    /** A file produced by a conversion job (shown in "My Files"). */
    RESULT("result"),
}

package uz.murodjon.filemaster.common

import com.fasterxml.jackson.annotation.JsonValue

/**
 * Brand category token — drives tile/chip color on the front-end.
 * [folder] is the storage sub-directory every file of this category is stored under
 * (e.g. images -> `image/`, pdf/word -> `documents/`, audio -> `audio/`).
 */
enum class CategoryToken(@get:JsonValue val token: String, val folder: String) {
    PDF("pdf", "documents"),
    DOC("doc", "documents"),
    SHEET("sheet", "documents"),
    SLIDE("slide", "documents"),
    IMAGE("image", "image"),
    AUDIO("audio", "audio"),
    VIDEO("video", "video"),
    ARCHIVE("archive", "archive");

    companion object {
        fun from(value: String): CategoryToken =
            entries.firstOrNull { it.token.equals(value, ignoreCase = true) }
                ?: throw IllegalArgumentException("Unknown category: $value")
    }
}

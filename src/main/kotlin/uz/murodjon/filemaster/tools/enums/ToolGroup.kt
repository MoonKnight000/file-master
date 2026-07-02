package uz.murodjon.filemaster.tools.enums

import uz.murodjon.filemaster.common.CategoryToken

/** Coarse grouping of tools into the per-category controllers (audio/video/image/document/archive). */
enum class ToolGroup(val value: String) {
    AUDIO("audio"),
    VIDEO("video"),
    IMAGE("image"),
    DOCUMENT("document"),
    ARCHIVE("archive");

    /** The category tokens that belong to this group (DOCUMENT spans pdf/doc/sheet/slide). */
    val categories: List<CategoryToken>
        get() = CategoryToken.entries.filter { of(it) == this }

    companion object {
        fun of(category: CategoryToken): ToolGroup = when (category) {
            CategoryToken.AUDIO -> AUDIO
            CategoryToken.VIDEO -> VIDEO
            CategoryToken.IMAGE -> IMAGE
            CategoryToken.ARCHIVE -> ARCHIVE
            CategoryToken.PDF, CategoryToken.DOC, CategoryToken.SHEET, CategoryToken.SLIDE -> DOCUMENT
        }
    }
}

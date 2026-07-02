package uz.murodjon.filemaster.common

/** Mirror of the front-end `EXT_MAP` (src/lib/fileTypes.ts). Maps an extension to a category. */
object FileCategories {

    private val EXT_MAP: Map<String, CategoryToken> = buildMap {
        put("pdf", CategoryToken.PDF)
        listOf("doc", "docx", "txt", "rtf", "odt", "pages", "djvu").forEach { put(it, CategoryToken.DOC) }
        listOf("xls", "xlsx", "csv", "numbers", "ods").forEach { put(it, CategoryToken.SHEET) }
        listOf("ppt", "pptx", "key", "odp").forEach { put(it, CategoryToken.SLIDE) }
        listOf("png", "jpg", "jpeg", "gif", "webp", "svg", "heic", "bmp", "tiff").forEach { put(it, CategoryToken.IMAGE) }
        listOf("mp3", "wav", "flac", "aac", "m4a", "ogg", "wma").forEach { put(it, CategoryToken.AUDIO) }
        listOf("mp4", "mov", "avi", "mkv", "webm").forEach { put(it, CategoryToken.VIDEO) }
        listOf("zip", "rar", "7z", "tar", "gz").forEach { put(it, CategoryToken.ARCHIVE) }
    }

    fun extOf(filename: String): String =
        filename.substringAfterLast('.', "").lowercase()

    fun categoryOf(filename: String): CategoryToken =
        EXT_MAP[extOf(filename)] ?: CategoryToken.DOC
}

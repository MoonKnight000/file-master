package uz.murodjon.filemaster.tools.enums

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue

enum class ToolSlug(@get:JsonValue val slug: String) {
    DJVU_TO_PDF("djvu-to-pdf"),
    PDF_TO_WORD("pdf-to-word"),
    WORD_TO_PDF("word-to-pdf"),
    COMPRESS_PDF("compress-pdf"),
    MERGE_PDF("merge-pdf"),
    COMPRESS_IMAGE("compress-image"),
    CONVERT_IMAGE("convert-image"),
    CONVERT_AUDIO("convert-audio"),
    CONVERT_VIDEO("convert-video"),
    EXCEL_TO_PDF("excel-to-pdf"),
    PPT_TO_PDF("ppt-to-pdf"),
    UNZIP_FILES("unzip-files"),
    OCR_SCAN("ocr-scan"),
    PDF_TO_IMAGES("pdf-to-images"),
    IMAGES_TO_PDF("images-to-pdf"),
    ZIP_FILES("zip-files"),
    COMPRESS_VIDEO("compress-video"),
    VIDEO_TO_AUDIO("video-to-audio"),
    RESIZE_IMAGE("resize-image"),
    ROTATE_IMAGE("rotate-image"),
    ROTATE_PDF("rotate-pdf"),
    SPLIT_PDF("split-pdf"),
    TRIM_AUDIO("trim-audio"),
    TRIM_VIDEO("trim-video");

    companion object {
        @JsonCreator
        @JvmStatic
        fun from(slug: String): ToolSlug =
            entries.firstOrNull { it.slug == slug }
                ?: throw IllegalArgumentException("Unknown tool slug: '$slug'")
    }
}

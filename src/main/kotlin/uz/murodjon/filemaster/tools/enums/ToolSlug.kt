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
    TRIM_VIDEO("trim-video"),
    DELETE_PDF_PAGES("delete-pdf-pages"),
    EXTRACT_PDF_PAGES("extract-pdf-pages"),
    REORDER_PDF_PAGES("reorder-pdf-pages"),
    WATERMARK_PDF("watermark-pdf"),
    PAGE_NUMBERS_PDF("page-numbers-pdf"),
    PROTECT_PDF("protect-pdf"),
    UNLOCK_PDF("unlock-pdf"),
    CROP_IMAGE("crop-image"),
    FLIP_IMAGE("flip-image"),
    WATERMARK_IMAGE("watermark-image"),
    IMAGE_FILTER("image-filter"),
    ADJUST_IMAGE("adjust-image"),
    CROP_VIDEO("crop-video"),
    ROTATE_VIDEO("rotate-video"),
    FLIP_VIDEO("flip-video"),
    SPEED_VIDEO("speed-video"),
    MUTE_VIDEO("mute-video"),
    WATERMARK_VIDEO("watermark-video"),
    MERGE_VIDEOS("merge-videos"),
    VIDEO_TO_GIF("video-to-gif"),
    MERGE_AUDIO("merge-audio"),
    SPEED_AUDIO("speed-audio"),
    VOLUME_AUDIO("volume-audio"),
    FADE_AUDIO("fade-audio"),
    REVERSE_AUDIO("reverse-audio"),
    NORMALIZE_AUDIO("normalize-audio"),
    REMOVE_BACKGROUND("remove-background"),
    AUDIO_TO_TEXT("audio-to-text");

    companion object {
        @JsonCreator
        @JvmStatic
        fun from(slug: String): ToolSlug =
            entries.firstOrNull { it.slug == slug }
                ?: throw IllegalArgumentException("Unknown tool slug: '$slug'")
    }
}

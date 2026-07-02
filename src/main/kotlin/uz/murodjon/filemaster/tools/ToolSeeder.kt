package uz.murodjon.filemaster.tools

import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import uz.murodjon.filemaster.common.CategoryToken
import uz.murodjon.filemaster.common.FileFormat
import uz.murodjon.filemaster.tools.dto.ToolDef
import uz.murodjon.filemaster.tools.enums.EditOperation
import uz.murodjon.filemaster.tools.enums.ToolBadge
import uz.murodjon.filemaster.tools.enums.ToolEngine
import uz.murodjon.filemaster.tools.enums.ToolKind
import uz.murodjon.filemaster.tools.model.Tool
import uz.murodjon.filemaster.tools.repository.ToolRepository
import java.time.Instant

/**
 * Seeds (and keeps in sync) the `tools` table from the canonical catalog below — the single
 * source of truth, mirroring the front-end's `src/data/tools.ts`. Runs on every startup and
 * upserts by slug, so editing a definition here propagates to the DB on the next boot.
 */
@Component
class ToolSeeder(private val tools: ToolRepository) : ApplicationRunner {

    private val log = LoggerFactory.getLogger(javaClass)

    @Transactional
    override fun run(args: ApplicationArguments) {
        var created = 0
        var updated = 0
        catalog.forEach { def ->
            val existing = tools.findBySlug(def.slug)
            if (existing == null) {
                tools.save(def.toNewEntity())
                created++
            } else {
                existing.applyFrom(def)
                tools.save(existing)
                updated++
            }
        }
        log.info("Tool catalog seeded: {} created, {} updated, {} total.", created, updated, catalog.size)
    }

    private fun ToolDef.toNewEntity() = Tool(
        slug = slug,
        title = title,
        description = desc,
        category = category,
        icon = icon,
        badge = badge,
        engine = engine,
        kind = kind,
        defaultFormat = defaultFormat,
        editOperation = editOperation,
        keepOriginalDefault = keepOriginalDefault,
        mergeIntoOneDefault = mergeIntoOneDefault,
        accept = accept.toMutableList(),
        outputFormats = outputFormats.toMutableList(),
    )

    private fun Tool.applyFrom(def: ToolDef) {
        title = def.title
        description = def.desc
        category = def.category
        icon = def.icon
        badge = def.badge
        engine = def.engine
        kind = def.kind
        defaultFormat = def.defaultFormat
        editOperation = def.editOperation
        keepOriginalDefault = def.keepOriginalDefault
        mergeIntoOneDefault = def.mergeIntoOneDefault
        accept = def.accept.toMutableList()
        outputFormats = def.outputFormats.toMutableList()
        active = true
        updatedTimestamp = Instant.now().epochSecond
    }

    private val catalog: List<ToolDef> = listOf(
        ToolDef("djvu-to-pdf", "DjVu to PDF", "Convert DjVu scans into standard PDF files.",
            CategoryToken.DOC, "file-scan",
            accept = listOf(".djvu"),
            outputFormats = listOf(FileFormat.PDF),
            defaultFormat = FileFormat.PDF, engine = ToolEngine.DJVU),

        ToolDef("pdf-to-word", "PDF to Word", "Turn PDFs into editable Word documents.",
            CategoryToken.PDF, "file-text", ToolBadge.POPULAR,
            accept = listOf(".pdf"),
            outputFormats = listOf(FileFormat.DOCX, FileFormat.DOC, FileFormat.TXT, FileFormat.ODT),
            defaultFormat = FileFormat.DOCX, engine = ToolEngine.LIBREOFFICE),

        ToolDef("word-to-pdf", "Word to PDF", "Convert DOC & DOCX to polished PDFs.",
            CategoryToken.DOC, "file-type",
            accept = listOf(".doc", ".docx", ".odt", ".rtf", ".txt"),
            outputFormats = listOf(FileFormat.PDF),
            defaultFormat = FileFormat.PDF, engine = ToolEngine.LIBREOFFICE),

        ToolDef("compress-pdf", "Compress PDF", "Shrink large PDFs while keeping quality.",
            CategoryToken.PDF, "minimize-2",
            accept = listOf(".pdf"),
            outputFormats = listOf(FileFormat.PDF),
            defaultFormat = FileFormat.PDF, engine = ToolEngine.GHOSTSCRIPT, kind = ToolKind.COMPRESS),

        ToolDef("merge-pdf", "Merge PDF", "Combine several PDFs into one file.",
            CategoryToken.PDF, "combine",
            accept = listOf(".pdf"),
            outputFormats = listOf(FileFormat.PDF),
            defaultFormat = FileFormat.PDF, engine = ToolEngine.PDF_MERGE, kind = ToolKind.MERGE,
            mergeIntoOneDefault = true),

        ToolDef("compress-image", "Compress Image", "Reduce JPG & PNG size, lossless.",
            CategoryToken.IMAGE, "image",
            accept = listOf(".jpg", ".jpeg", ".png"),
            outputFormats = listOf(FileFormat.JPG, FileFormat.PNG),
            defaultFormat = FileFormat.JPG, engine = ToolEngine.IMAGE, kind = ToolKind.COMPRESS),

        ToolDef("convert-image", "Convert Image", "JPG, PNG, WEBP, HEIC and more.",
            CategoryToken.IMAGE, "crop",
            accept = listOf(".jpg", ".jpeg", ".png", ".webp", ".bmp", ".gif", ".tiff", ".heic", ".heif"),
            outputFormats = listOf(FileFormat.PNG, FileFormat.JPG, FileFormat.WEBP, FileFormat.BMP),
            defaultFormat = FileFormat.PNG, engine = ToolEngine.FFMPEG),

        ToolDef("convert-audio", "Convert Audio", "MP3, WAV, FLAC, AAC and more.",
            CategoryToken.AUDIO, "music", ToolBadge.NEW,
            accept = listOf(".mp3", ".wav", ".flac", ".aac", ".m4a", ".ogg", ".wma"),
            outputFormats = listOf(FileFormat.MP3, FileFormat.WAV, FileFormat.FLAC, FileFormat.AAC, FileFormat.M4A, FileFormat.OGG),
            defaultFormat = FileFormat.MP3, engine = ToolEngine.FFMPEG),

        ToolDef("convert-video", "Convert Video", "MP4, MOV, WEBM at any quality.",
            CategoryToken.VIDEO, "video",
            accept = listOf(".mp4", ".mov", ".avi", ".mkv", ".webm"),
            outputFormats = listOf(FileFormat.MP4, FileFormat.MOV, FileFormat.WEBM, FileFormat.MKV),
            defaultFormat = FileFormat.MP4, engine = ToolEngine.FFMPEG),

        ToolDef("excel-to-pdf", "Excel to PDF", "Spreadsheets into shareable PDFs.",
            CategoryToken.SHEET, "sheet",
            accept = listOf(".xls", ".xlsx", ".csv", ".ods"),
            outputFormats = listOf(FileFormat.PDF),
            defaultFormat = FileFormat.PDF, engine = ToolEngine.LIBREOFFICE),

        ToolDef("ppt-to-pdf", "PPT to PDF", "Slides into portable PDF decks.",
            CategoryToken.SLIDE, "presentation",
            accept = listOf(".ppt", ".pptx", ".odp"),
            outputFormats = listOf(FileFormat.PDF),
            defaultFormat = FileFormat.PDF, engine = ToolEngine.LIBREOFFICE),

        ToolDef("unzip-files", "Unzip files", "Extract the contents of a ZIP archive.",
            CategoryToken.ARCHIVE, "folder-archive",
            accept = listOf(".zip"),
            outputFormats = listOf(FileFormat.ZIP),
            defaultFormat = FileFormat.ZIP, engine = ToolEngine.UNZIP, kind = ToolKind.UNZIP),

        ToolDef("ocr-scan", "OCR Scan", "Pull text out of scans & photos.",
            CategoryToken.DOC, "scan-text",
            accept = listOf(".png", ".jpg", ".jpeg"),
            outputFormats = listOf(FileFormat.TXT, FileFormat.PDF),
            defaultFormat = FileFormat.TXT, engine = ToolEngine.OCR, kind = ToolKind.OCR),

        // ── New tools (2026-06-25): PDF wave, media easy-wins, and the Edit mode ─────────────

        ToolDef("pdf-to-images", "PDF to Images", "Turn each PDF page into a JPG or PNG.",
            CategoryToken.PDF, "image",
            accept = listOf(".pdf"),
            outputFormats = listOf(FileFormat.JPG, FileFormat.PNG),
            defaultFormat = FileFormat.JPG, engine = ToolEngine.PDF_RASTER),

        ToolDef("images-to-pdf", "Images to PDF", "Combine images into a single PDF.",
            CategoryToken.PDF, "file-image", ToolBadge.NEW,
            accept = listOf(".jpg", ".jpeg", ".png", ".webp", ".bmp", ".gif", ".tiff"),
            outputFormats = listOf(FileFormat.PDF),
            defaultFormat = FileFormat.PDF, engine = ToolEngine.IMAGES_TO_PDF, kind = ToolKind.MERGE,
            mergeIntoOneDefault = true),

        ToolDef("zip-files", "Zip files", "Pack several files into one ZIP archive.",
            CategoryToken.ARCHIVE, "folder-archive",
            accept = listOf("*"),
            outputFormats = listOf(FileFormat.ZIP),
            defaultFormat = FileFormat.ZIP, engine = ToolEngine.ZIP_CREATE, kind = ToolKind.MERGE,
            mergeIntoOneDefault = true),

        ToolDef("compress-video", "Compress Video", "Shrink video size, keep it watchable.",
            CategoryToken.VIDEO, "minimize-2",
            accept = listOf(".mp4", ".mov", ".avi", ".mkv", ".webm"),
            outputFormats = listOf(FileFormat.MP4, FileFormat.MOV, FileFormat.WEBM, FileFormat.MKV),
            defaultFormat = FileFormat.MP4, engine = ToolEngine.FFMPEG, kind = ToolKind.COMPRESS),

        ToolDef("video-to-audio", "Video to Audio", "Extract the soundtrack from a video.",
            CategoryToken.AUDIO, "music",
            accept = listOf(".mp4", ".mov", ".avi", ".mkv", ".webm"),
            outputFormats = listOf(FileFormat.MP3, FileFormat.WAV, FileFormat.AAC, FileFormat.M4A),
            defaultFormat = FileFormat.MP3, engine = ToolEngine.FFMPEG),

        ToolDef("resize-image", "Resize Image", "Change image dimensions (px).",
            CategoryToken.IMAGE, "scaling",
            accept = listOf(".jpg", ".jpeg", ".png"),
            outputFormats = listOf(FileFormat.JPG, FileFormat.PNG),
            defaultFormat = FileFormat.JPG, engine = ToolEngine.IMAGE, kind = ToolKind.EDIT,
            editOperation = EditOperation.RESIZE),

        ToolDef("rotate-image", "Rotate Image", "Rotate an image 90/180/270 degrees.",
            CategoryToken.IMAGE, "rotate-cw",
            accept = listOf(".jpg", ".jpeg", ".png"),
            outputFormats = listOf(FileFormat.JPG, FileFormat.PNG),
            defaultFormat = FileFormat.JPG, engine = ToolEngine.IMAGE, kind = ToolKind.EDIT,
            editOperation = EditOperation.ROTATE),

        ToolDef("rotate-pdf", "Rotate PDF", "Rotate every page of a PDF.",
            CategoryToken.PDF, "rotate-cw",
            accept = listOf(".pdf"),
            outputFormats = listOf(FileFormat.PDF),
            defaultFormat = FileFormat.PDF, engine = ToolEngine.PDF_EDIT, kind = ToolKind.EDIT,
            editOperation = EditOperation.ROTATE),

        ToolDef("split-pdf", "Split PDF", "Split a PDF into pages or ranges.",
            CategoryToken.PDF, "scissors",
            accept = listOf(".pdf"),
            outputFormats = listOf(FileFormat.PDF),
            defaultFormat = FileFormat.PDF, engine = ToolEngine.PDF_EDIT, kind = ToolKind.EDIT,
            editOperation = EditOperation.SPLIT),

        ToolDef("trim-audio", "Trim Audio", "Cut a clip out of an audio file.",
            CategoryToken.AUDIO, "scissors",
            accept = listOf(".mp3", ".wav", ".flac", ".aac", ".m4a", ".ogg", ".wma"),
            outputFormats = listOf(FileFormat.MP3, FileFormat.WAV, FileFormat.FLAC, FileFormat.AAC, FileFormat.M4A, FileFormat.OGG),
            defaultFormat = FileFormat.MP3, engine = ToolEngine.FFMPEG, kind = ToolKind.EDIT,
            editOperation = EditOperation.TRIM),

        ToolDef("trim-video", "Trim Video", "Cut a clip out of a video file.",
            CategoryToken.VIDEO, "scissors",
            accept = listOf(".mp4", ".mov", ".avi", ".mkv", ".webm"),
            outputFormats = listOf(FileFormat.MP4, FileFormat.MOV, FileFormat.WEBM, FileFormat.MKV),
            defaultFormat = FileFormat.MP4, engine = ToolEngine.FFMPEG, kind = ToolKind.EDIT,
            editOperation = EditOperation.TRIM),

        // ── New tools (2026-07-02): the full PDF & image edit wave ───────────────────────────

        ToolDef("delete-pdf-pages", "Delete PDF Pages", "Remove selected pages from a PDF.",
            CategoryToken.PDF, "file-minus", ToolBadge.NEW,
            accept = listOf(".pdf"),
            outputFormats = listOf(FileFormat.PDF),
            defaultFormat = FileFormat.PDF, engine = ToolEngine.PDF_EDIT, kind = ToolKind.EDIT,
            editOperation = EditOperation.DELETE_PAGES),

        ToolDef("extract-pdf-pages", "Extract PDF Pages", "Pull selected pages into a new PDF.",
            CategoryToken.PDF, "file-output", ToolBadge.NEW,
            accept = listOf(".pdf"),
            outputFormats = listOf(FileFormat.PDF),
            defaultFormat = FileFormat.PDF, engine = ToolEngine.PDF_EDIT, kind = ToolKind.EDIT,
            editOperation = EditOperation.EXTRACT_PAGES),

        ToolDef("reorder-pdf-pages", "Reorder PDF Pages", "Rearrange the pages of a PDF.",
            CategoryToken.PDF, "arrow-up-down", ToolBadge.NEW,
            accept = listOf(".pdf"),
            outputFormats = listOf(FileFormat.PDF),
            defaultFormat = FileFormat.PDF, engine = ToolEngine.PDF_EDIT, kind = ToolKind.EDIT,
            editOperation = EditOperation.REORDER_PAGES),

        ToolDef("watermark-pdf", "Watermark PDF", "Stamp a text watermark on every page.",
            CategoryToken.PDF, "stamp", ToolBadge.NEW,
            accept = listOf(".pdf"),
            outputFormats = listOf(FileFormat.PDF),
            defaultFormat = FileFormat.PDF, engine = ToolEngine.PDF_EDIT, kind = ToolKind.EDIT,
            editOperation = EditOperation.WATERMARK),

        ToolDef("page-numbers-pdf", "Page Numbers", "Add page numbers to a PDF.",
            CategoryToken.PDF, "list-ordered", ToolBadge.NEW,
            accept = listOf(".pdf"),
            outputFormats = listOf(FileFormat.PDF),
            defaultFormat = FileFormat.PDF, engine = ToolEngine.PDF_EDIT, kind = ToolKind.EDIT,
            editOperation = EditOperation.PAGE_NUMBERS),

        ToolDef("protect-pdf", "Protect PDF", "Lock a PDF with a password.",
            CategoryToken.PDF, "lock", ToolBadge.NEW,
            accept = listOf(".pdf"),
            outputFormats = listOf(FileFormat.PDF),
            defaultFormat = FileFormat.PDF, engine = ToolEngine.PDF_EDIT, kind = ToolKind.EDIT,
            editOperation = EditOperation.PROTECT),

        ToolDef("unlock-pdf", "Unlock PDF", "Remove the password from a PDF.",
            CategoryToken.PDF, "lock-open", ToolBadge.NEW,
            accept = listOf(".pdf"),
            outputFormats = listOf(FileFormat.PDF),
            defaultFormat = FileFormat.PDF, engine = ToolEngine.PDF_EDIT, kind = ToolKind.EDIT,
            editOperation = EditOperation.UNLOCK),

        ToolDef("crop-image", "Crop Image", "Cut a rectangle out of an image.",
            CategoryToken.IMAGE, "crop", ToolBadge.NEW,
            accept = listOf(".jpg", ".jpeg", ".png"),
            outputFormats = listOf(FileFormat.JPG, FileFormat.PNG),
            defaultFormat = FileFormat.JPG, engine = ToolEngine.IMAGE, kind = ToolKind.EDIT,
            editOperation = EditOperation.CROP),

        ToolDef("flip-image", "Flip Image", "Mirror an image horizontally or vertically.",
            CategoryToken.IMAGE, "flip-horizontal", ToolBadge.NEW,
            accept = listOf(".jpg", ".jpeg", ".png"),
            outputFormats = listOf(FileFormat.JPG, FileFormat.PNG),
            defaultFormat = FileFormat.JPG, engine = ToolEngine.IMAGE, kind = ToolKind.EDIT,
            editOperation = EditOperation.FLIP),

        ToolDef("watermark-image", "Watermark Image", "Stamp a text watermark on an image.",
            CategoryToken.IMAGE, "stamp", ToolBadge.NEW,
            accept = listOf(".jpg", ".jpeg", ".png"),
            outputFormats = listOf(FileFormat.JPG, FileFormat.PNG),
            defaultFormat = FileFormat.JPG, engine = ToolEngine.IMAGE, kind = ToolKind.EDIT,
            editOperation = EditOperation.WATERMARK),

        ToolDef("image-filter", "Image Filters", "Grayscale, sepia, invert, blur or sharpen.",
            CategoryToken.IMAGE, "wand-2", ToolBadge.NEW,
            accept = listOf(".jpg", ".jpeg", ".png"),
            outputFormats = listOf(FileFormat.JPG, FileFormat.PNG),
            defaultFormat = FileFormat.JPG, engine = ToolEngine.IMAGE, kind = ToolKind.EDIT,
            editOperation = EditOperation.FILTER),

        ToolDef("adjust-image", "Adjust Image", "Brightness, contrast and saturation.",
            CategoryToken.IMAGE, "sliders-horizontal", ToolBadge.NEW,
            accept = listOf(".jpg", ".jpeg", ".png"),
            outputFormats = listOf(FileFormat.JPG, FileFormat.PNG),
            defaultFormat = FileFormat.JPG, engine = ToolEngine.IMAGE, kind = ToolKind.EDIT,
            editOperation = EditOperation.ADJUST),

        // ── New tools (2026-07-02, wave 2): the video & audio edit wave ──────────────────────

        ToolDef("crop-video", "Crop Video", "Cut a rectangle out of the video frame.",
            CategoryToken.VIDEO, "crop", ToolBadge.NEW,
            accept = listOf(".mp4", ".mov", ".avi", ".mkv", ".webm"),
            outputFormats = listOf(FileFormat.MP4, FileFormat.MOV, FileFormat.WEBM, FileFormat.MKV),
            defaultFormat = FileFormat.MP4, engine = ToolEngine.FFMPEG, kind = ToolKind.EDIT,
            editOperation = EditOperation.CROP),

        ToolDef("rotate-video", "Rotate Video", "Rotate a video 90/180/270 degrees.",
            CategoryToken.VIDEO, "rotate-cw", ToolBadge.NEW,
            accept = listOf(".mp4", ".mov", ".avi", ".mkv", ".webm"),
            outputFormats = listOf(FileFormat.MP4, FileFormat.MOV, FileFormat.WEBM, FileFormat.MKV),
            defaultFormat = FileFormat.MP4, engine = ToolEngine.FFMPEG, kind = ToolKind.EDIT,
            editOperation = EditOperation.ROTATE),

        ToolDef("flip-video", "Flip Video", "Mirror a video horizontally or vertically.",
            CategoryToken.VIDEO, "flip-horizontal", ToolBadge.NEW,
            accept = listOf(".mp4", ".mov", ".avi", ".mkv", ".webm"),
            outputFormats = listOf(FileFormat.MP4, FileFormat.MOV, FileFormat.WEBM, FileFormat.MKV),
            defaultFormat = FileFormat.MP4, engine = ToolEngine.FFMPEG, kind = ToolKind.EDIT,
            editOperation = EditOperation.FLIP),

        ToolDef("speed-video", "Change Video Speed", "Speed a video up or slow it down (0.25x–4x).",
            CategoryToken.VIDEO, "gauge", ToolBadge.NEW,
            accept = listOf(".mp4", ".mov", ".avi", ".mkv", ".webm"),
            outputFormats = listOf(FileFormat.MP4, FileFormat.MOV, FileFormat.WEBM, FileFormat.MKV),
            defaultFormat = FileFormat.MP4, engine = ToolEngine.FFMPEG, kind = ToolKind.EDIT,
            editOperation = EditOperation.SPEED),

        ToolDef("mute-video", "Mute Video", "Strip the audio track from a video.",
            CategoryToken.VIDEO, "volume-x", ToolBadge.NEW,
            accept = listOf(".mp4", ".mov", ".avi", ".mkv", ".webm"),
            outputFormats = listOf(FileFormat.MP4, FileFormat.MOV, FileFormat.WEBM, FileFormat.MKV),
            defaultFormat = FileFormat.MP4, engine = ToolEngine.FFMPEG, kind = ToolKind.EDIT,
            editOperation = EditOperation.MUTE),

        ToolDef("watermark-video", "Watermark Video", "Stamp a text watermark on a video.",
            CategoryToken.VIDEO, "stamp", ToolBadge.NEW,
            accept = listOf(".mp4", ".mov", ".avi", ".mkv", ".webm"),
            outputFormats = listOf(FileFormat.MP4, FileFormat.MOV, FileFormat.WEBM, FileFormat.MKV),
            defaultFormat = FileFormat.MP4, engine = ToolEngine.FFMPEG, kind = ToolKind.EDIT,
            editOperation = EditOperation.WATERMARK),

        ToolDef("merge-videos", "Merge Videos", "Join several videos into one.",
            CategoryToken.VIDEO, "combine", ToolBadge.NEW,
            accept = listOf(".mp4", ".mov", ".avi", ".mkv", ".webm"),
            outputFormats = listOf(FileFormat.MP4, FileFormat.MOV, FileFormat.WEBM, FileFormat.MKV),
            defaultFormat = FileFormat.MP4, engine = ToolEngine.FFMPEG, kind = ToolKind.MERGE,
            mergeIntoOneDefault = true),

        ToolDef("video-to-gif", "Video to GIF", "Turn a video clip into an animated GIF.",
            CategoryToken.VIDEO, "film", ToolBadge.NEW,
            accept = listOf(".mp4", ".mov", ".avi", ".mkv", ".webm"),
            outputFormats = listOf(FileFormat.GIF),
            defaultFormat = FileFormat.GIF, engine = ToolEngine.FFMPEG),

        ToolDef("merge-audio", "Merge Audio", "Join several audio files into one.",
            CategoryToken.AUDIO, "combine", ToolBadge.NEW,
            accept = listOf(".mp3", ".wav", ".flac", ".aac", ".m4a", ".ogg", ".wma"),
            outputFormats = listOf(FileFormat.MP3, FileFormat.WAV, FileFormat.FLAC, FileFormat.AAC, FileFormat.M4A, FileFormat.OGG),
            defaultFormat = FileFormat.MP3, engine = ToolEngine.FFMPEG, kind = ToolKind.MERGE,
            mergeIntoOneDefault = true),

        ToolDef("speed-audio", "Change Audio Speed", "Speed audio up or slow it down (0.25x–4x).",
            CategoryToken.AUDIO, "gauge", ToolBadge.NEW,
            accept = listOf(".mp3", ".wav", ".flac", ".aac", ".m4a", ".ogg", ".wma"),
            outputFormats = listOf(FileFormat.MP3, FileFormat.WAV, FileFormat.FLAC, FileFormat.AAC, FileFormat.M4A, FileFormat.OGG),
            defaultFormat = FileFormat.MP3, engine = ToolEngine.FFMPEG, kind = ToolKind.EDIT,
            editOperation = EditOperation.SPEED),

        ToolDef("volume-audio", "Change Volume", "Make audio louder or quieter (0x–4x).",
            CategoryToken.AUDIO, "volume-2", ToolBadge.NEW,
            accept = listOf(".mp3", ".wav", ".flac", ".aac", ".m4a", ".ogg", ".wma"),
            outputFormats = listOf(FileFormat.MP3, FileFormat.WAV, FileFormat.FLAC, FileFormat.AAC, FileFormat.M4A, FileFormat.OGG),
            defaultFormat = FileFormat.MP3, engine = ToolEngine.FFMPEG, kind = ToolKind.EDIT,
            editOperation = EditOperation.VOLUME),

        ToolDef("fade-audio", "Fade Audio", "Add a fade-in and/or fade-out.",
            CategoryToken.AUDIO, "audio-waveform", ToolBadge.NEW,
            accept = listOf(".mp3", ".wav", ".flac", ".aac", ".m4a", ".ogg", ".wma"),
            outputFormats = listOf(FileFormat.MP3, FileFormat.WAV, FileFormat.FLAC, FileFormat.AAC, FileFormat.M4A, FileFormat.OGG),
            defaultFormat = FileFormat.MP3, engine = ToolEngine.FFMPEG, kind = ToolKind.EDIT,
            editOperation = EditOperation.FADE),

        ToolDef("reverse-audio", "Reverse Audio", "Play an audio file backwards.",
            CategoryToken.AUDIO, "undo-2", ToolBadge.NEW,
            accept = listOf(".mp3", ".wav", ".flac", ".aac", ".m4a", ".ogg", ".wma"),
            outputFormats = listOf(FileFormat.MP3, FileFormat.WAV, FileFormat.FLAC, FileFormat.AAC, FileFormat.M4A, FileFormat.OGG),
            defaultFormat = FileFormat.MP3, engine = ToolEngine.FFMPEG, kind = ToolKind.EDIT,
            editOperation = EditOperation.REVERSE),

        ToolDef("normalize-audio", "Normalize Audio", "Even out the loudness (EBU loudnorm).",
            CategoryToken.AUDIO, "activity", ToolBadge.NEW,
            accept = listOf(".mp3", ".wav", ".flac", ".aac", ".m4a", ".ogg", ".wma"),
            outputFormats = listOf(FileFormat.MP3, FileFormat.WAV, FileFormat.FLAC, FileFormat.AAC, FileFormat.M4A, FileFormat.OGG),
            defaultFormat = FileFormat.MP3, engine = ToolEngine.FFMPEG, kind = ToolKind.EDIT,
            editOperation = EditOperation.NORMALIZE),
    )
}

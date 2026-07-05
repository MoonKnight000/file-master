package uz.murodjon.filemaster.common

import com.fasterxml.jackson.annotation.JsonValue

/**
 * The output formats a tool can produce / a job can target. Stored UPPERCASE (the token also
 * doubles as the file extension, e.g. `PDF` -> `.pdf`). The conversion pipeline still passes
 * the format around as a plain string (`ConversionJob.outputFormat`); this enum constrains the
 * tool catalog so a tool can only advertise a known format.
 */
enum class FileFormat(@get:JsonValue val value: String) {
    // Documents
    PDF("PDF"),
    DOCX("DOCX"),
    DOC("DOC"),
    ODT("ODT"),
    TXT("TXT"),
    // Images
    JPG("JPG"),
    PNG("PNG"),
    WEBP("WEBP"),
    BMP("BMP"),
    GIF("GIF"),
    // Audio
    MP3("MP3"),
    WAV("WAV"),
    FLAC("FLAC"),
    AAC("AAC"),
    M4A("M4A"),
    OGG("OGG"),
    // Video
    MP4("MP4"),
    MOV("MOV"),
    WEBM("WEBM"),
    MKV("MKV"),
    // Subtitles / transcripts
    SRT("SRT"),
    VTT("VTT"),
    // Archive
    ZIP("ZIP");

    companion object {
        fun from(value: String): FileFormat =
            entries.firstOrNull { it.value.equals(value, ignoreCase = true) }
                ?: throw IllegalArgumentException("Unknown format: $value")
    }
}

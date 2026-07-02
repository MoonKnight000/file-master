package uz.murodjon.filemaster.files.enums

/** Where a [uz.murodjon.filemaster.files.model.StoredFile] came from. */
enum class FileSource {
    /** A source file the user uploaded (conversion input). */
    UPLOAD,

    /** A file produced by a conversion job (shown in "My Files"). */
    RESULT,
}

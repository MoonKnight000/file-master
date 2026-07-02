package uz.murodjon.filemaster.files.dto

import java.io.OutputStream

/** A file ready to be streamed back to the client. */
class FileDownload(
    val name: String,
    val contentType: String,
    val bytes: Long,
    val writeTo: (OutputStream) -> Unit,
)

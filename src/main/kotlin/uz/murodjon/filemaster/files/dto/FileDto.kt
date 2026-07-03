package uz.murodjon.filemaster.files.dto

import uz.murodjon.filemaster.common.CategoryToken
import uz.murodjon.filemaster.files.enums.FileSource
import uz.murodjon.filemaster.files.model.StoredFile

data class FileDto(
    val id: Long,
    val name: String,
    val bytes: Long,
    val category: CategoryToken,
    val source: FileSource,
    val status: String,
    val createdTimestamp: Long?,
    val expiresTimestamp: Long?,
    val format: String,
    val starred: Boolean,
    val absolutePath: String,
    val downloadUrl: String,
    /** Pixel dimensions for images/videos (probed at upload); null when unknown. */
    val width: Int? = null,
    val height: Int? = null,
) {
    constructor(file: StoredFile, retentionMinutes: Long) : this(
        id = file.id!!,
        name = file.originalName,
        bytes = file.bytes,
        category = file.category,
        source = file.source,
        status = "done",
        createdTimestamp = file.createdTimestamp,
        expiresTimestamp = if (file.source == FileSource.RESULT) {
            file.createdTimestamp?.plus(retentionMinutes * 60)
        } else {
            null
        },
        format = file.format,
        starred = file.starred,
        absolutePath = file.absolutePath,
        downloadUrl = "/v1/files/${file.id}/download",
        width = file.width,
        height = file.height,
    )
}

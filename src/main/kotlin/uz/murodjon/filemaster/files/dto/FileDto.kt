package uz.murodjon.filemaster.files.dto

import uz.murodjon.filemaster.files.enums.FileSource
import uz.murodjon.filemaster.files.model.StoredFile

data class FileDto(
    val id: Long,
    val name: String,
    val bytes: Long,
    val category: String,
    val source: String,
    val status: String,
    val createdTimestamp: Long?,
    val expiresTimestamp: Long?,
    val format: String,
    val starred: Boolean,
    val absolutePath: String,
    val downloadUrl: String
) {
    constructor(file: StoredFile, retentionMinutes: Long) : this(
        id = file.id!!,
        name = file.originalName,
        bytes = file.bytes,
        category = file.category.token,
        source = file.source.name.lowercase(),
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
    )
}

package uz.murodjon.filemaster.conversion.dto

import uz.murodjon.filemaster.conversion.model.JobFile

data class JobFileDto(
    val fileId: Long,
    val name: String,
    val status: String,
    val progress: Int,
    val resultId: Long? = null,
    val bytes: Long? = null,
    val downloadUrl: String? = null,
) {
    constructor(jobFile: JobFile) : this(
        fileId = jobFile.upload.id!!,
        name = jobFile.name,
        status = jobFile.status.value,
        progress = jobFile.progress,
        resultId = jobFile.result?.id,
        bytes = jobFile.bytes,
        downloadUrl = jobFile.result?.id?.let { "/v1/files/$it/download" },
    )
}

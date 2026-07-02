package uz.murodjon.filemaster.conversion.dto

import uz.murodjon.filemaster.conversion.model.ConversionJob
import uz.murodjon.filemaster.tools.enums.ToolSlug

data class JobDto(
    val jobId: Long,
    val tool: ToolSlug,
    val outputFormat: String,
    val status: String,
    val progress: Int,
    val createdTimestamp: Long?,
    val files: List<JobFileDto>,
) {
    constructor(job: ConversionJob) : this(
        jobId = job.id!!,
        tool = job.tool,
        outputFormat = job.outputFormat,
        status = job.status.value,
        progress = job.progress,
        createdTimestamp = job.createdTimestamp,
        files = job.files.map { JobFileDto(it) },
    )
}

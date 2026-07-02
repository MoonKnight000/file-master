package uz.murodjon.filemaster.archive.dto

import uz.murodjon.filemaster.tools.enums.ToolSlug

data class ArchiveConvertRequest(
    val tool: ToolSlug?,
    val fileIds: List<Long>?,
    val options: ArchiveConvertOptions?,
)

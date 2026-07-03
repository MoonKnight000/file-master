package uz.murodjon.filemaster.document.dto

import uz.murodjon.filemaster.tools.enums.ToolSlug

data class DocumentConvertRequest(
    val tool: ToolSlug?,
    val fileIds: List<Long>?,
    val options: DocumentConvertOptions?,
)

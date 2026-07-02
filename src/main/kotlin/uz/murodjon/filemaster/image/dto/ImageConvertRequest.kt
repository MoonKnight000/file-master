package uz.murodjon.filemaster.image.dto

import uz.murodjon.filemaster.tools.enums.ToolSlug

data class ImageConvertRequest(
    val tool: ToolSlug?,
    val fileIds: List<Long>?,
    val options: ImageConvertOptions?,
)

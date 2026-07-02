package uz.murodjon.filemaster.video.dto

import uz.murodjon.filemaster.tools.enums.ToolSlug

data class VideoConvertRequest(
    val tool: ToolSlug?,
    val fileIds: List<Long>?,
    val options: VideoConvertOptions?,
)

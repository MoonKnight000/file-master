package uz.murodjon.filemaster.audio.dto

import uz.murodjon.filemaster.tools.enums.ToolSlug

data class AudioConvertRequest(
    val tool: ToolSlug?,
    val fileIds: List<Long>?,
    val options: AudioConvertOptions?,
)

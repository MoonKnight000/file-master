package uz.murodjon.filemaster.tools.dto

data class UserToolsResponse(
    val favorites: List<ToolDef>,
    val recent: List<ToolDef>,
)

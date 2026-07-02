package uz.murodjon.filemaster.tools.dto

data class ToolDetail(
    val slug: String,
    val title: String,
    val desc: String,
    val category: String,
    val kind: String,
    val icon: String,
    val badge: String? = null,
    val accept: List<String>,
    val outputFormats: List<String>,
    val defaultFormat: String,
    val options: ToolOptions,
)

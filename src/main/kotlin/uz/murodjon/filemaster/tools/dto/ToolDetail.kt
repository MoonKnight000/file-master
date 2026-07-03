package uz.murodjon.filemaster.tools.dto

import uz.murodjon.filemaster.common.CategoryToken
import uz.murodjon.filemaster.common.FileFormat
import uz.murodjon.filemaster.tools.enums.ToolBadge
import uz.murodjon.filemaster.tools.enums.ToolKind

data class ToolDetail(
    val slug: String,
    val title: String,
    val desc: String,
    val category: CategoryToken,
    val kind: ToolKind,
    val icon: String,
    val badge: ToolBadge? = null,
    val accept: List<String>,
    val outputFormats: List<FileFormat>,
    val defaultFormat: FileFormat,
    /** SEO landing-page copy (2–3 sentences). */
    val longDescription: String,
    /** SEO landing-page FAQ (rendered + used for FAQ structured data). */
    val faq: List<ToolFaqEntry>,
    val options: ToolOptions,
)

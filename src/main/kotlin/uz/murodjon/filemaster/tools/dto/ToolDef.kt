package uz.murodjon.filemaster.tools.dto

import uz.murodjon.filemaster.common.CategoryToken
import uz.murodjon.filemaster.common.FileFormat
import uz.murodjon.filemaster.tools.enums.EditOperation
import uz.murodjon.filemaster.tools.enums.ToolBadge
import uz.murodjon.filemaster.tools.enums.ToolEngine
import uz.murodjon.filemaster.tools.enums.ToolKind

/**
 * A single tool definition in its in-memory, fully-loaded form. The persistent source of truth
 * is the `tools` table (see `Tool`); [uz.murodjon.filemaster.tools.service.ToolProvider] maps a
 * row into this so the conversion pipeline never touches JPA entities directly.
 */
data class ToolDef(
    val slug: String,
    val title: String,
    val desc: String,
    val category: CategoryToken,
    val icon: String,
    val badge: ToolBadge? = null,
    val accept: List<String>,
    val outputFormats: List<FileFormat>,
    val defaultFormat: FileFormat,
    val engine: ToolEngine,
    val kind: ToolKind = ToolKind.CONVERT,
    val keepOriginalDefault: Boolean = true,
    val mergeIntoOneDefault: Boolean = false,
    /** The interactive edit a `kind == EDIT` tool performs; null for every non-edit tool. */
    val editOperation: EditOperation? = null,
)

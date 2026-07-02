package uz.murodjon.filemaster.tools.dto

/**
 * A tool listing. [items] are full [ToolDef]s — enums stay as enums (Jackson serializes them via
 * their `@JsonValue`), so no fields are flattened to plain strings.
 */
data class ToolListResponse(
    val items: List<ToolDef>
)

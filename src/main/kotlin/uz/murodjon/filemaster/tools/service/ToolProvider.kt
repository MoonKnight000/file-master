package uz.murodjon.filemaster.tools.service

import uz.murodjon.filemaster.tools.dto.ToolDef
import uz.murodjon.filemaster.tools.enums.ToolGroup

/**
 * DB-backed read access to the tool catalog, returning fully-loaded [ToolDef]s (never JPA
 * entities) so callers — including the async conversion worker — are decoupled from Hibernate.
 */
interface ToolProvider {

    /** All active tools, in catalog order. */
    fun all(): List<ToolDef>

    /** Active tools in the given file-type [group]. */
    fun byGroup(group: ToolGroup): List<ToolDef>

    /** Active tools in [category] (`null`/"all" returns everything). */
    fun byCategory(category: String?): List<ToolDef>

    /** The tool with [slug], or null if there's no such (active) tool. */
    fun findBySlug(slug: String): ToolDef?
}

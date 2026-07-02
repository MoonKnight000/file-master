package uz.murodjon.filemaster.tools.model

import jakarta.persistence.CollectionTable
import jakarta.persistence.Column
import jakarta.persistence.ElementCollection
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.Table
import uz.murodjon.filemaster.common.CategoryToken
import uz.murodjon.filemaster.common.FileFormat
import uz.murodjon.filemaster.tools.enums.EditOperation
import uz.murodjon.filemaster.tools.enums.ToolBadge
import uz.murodjon.filemaster.tools.enums.ToolEngine
import uz.murodjon.filemaster.tools.enums.ToolKind
import java.time.Instant

/**
 * Persistent tool catalog row — the source of truth the front-end and the conversion pipeline
 * read from. Seeded on startup (see `ToolSeeder`). [accept] and [outputFormats] are side tables;
 * everything else is a column, with the discriminating fields ([category], [engine], [kind],
 * [badge], [defaultFormat]) stored as enum strings.
 */
@Entity
@Table(name = "tools")
class Tool(
    @Column(unique = true, length = 64) var slug: String,
    @Column(length = 128) var title: String,
    @Column(length = 512) var description: String,
    @Enumerated(EnumType.STRING) @Column(length = 16) var category: CategoryToken,
    @Column(length = 64) var icon: String,
    @Enumerated(EnumType.STRING) @Column(length = 16) var badge: ToolBadge? = null,
    @Enumerated(EnumType.STRING) @Column(length = 16) var engine: ToolEngine,
    @Enumerated(EnumType.STRING) @Column(length = 16) var kind: ToolKind = ToolKind.CONVERT,
    @Enumerated(EnumType.STRING) @Column(name = "default_format", length = 16) var defaultFormat: FileFormat,
    @Enumerated(EnumType.STRING) @Column(name = "edit_operation", length = 16) var editOperation: EditOperation? = null,
    var keepOriginalDefault: Boolean = true,
    var mergeIntoOneDefault: Boolean = false,

    @ElementCollection
    @CollectionTable(name = "tool_accept", joinColumns = [JoinColumn(name = "tool_id")])
    @Column(name = "ext", length = 16)
    var accept: MutableList<String> = mutableListOf(),

    @ElementCollection
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "tool_output_formats", joinColumns = [JoinColumn(name = "tool_id")])
    @Column(name = "format", length = 16)
    var outputFormats: MutableList<FileFormat> = mutableListOf(),
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    var active: Boolean = true
    var createdTimestamp: Long? = Instant.now().epochSecond
    var updatedTimestamp: Long? = Instant.now().epochSecond
}

package uz.murodjon.filemaster.tools.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import uz.murodjon.filemaster.auth.model.User
import java.time.Instant

@Entity
@Table(
    name = "user_tool_stats",
    uniqueConstraints = [UniqueConstraint(columnNames = ["user_id", "tool_slug"])],
)
class UserToolStat(
    @ManyToOne(fetch = FetchType.LAZY) var user: User,
    @Column(length = 64) var toolSlug: String,
    var favorited: Boolean = false,
    var lastUsedTimestamp: Long? = null,
    var useCount: Long = 0,
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    var createdTimestamp: Long? = Instant.now().epochSecond
    var updatedTimestamp: Long? = Instant.now().epochSecond
}

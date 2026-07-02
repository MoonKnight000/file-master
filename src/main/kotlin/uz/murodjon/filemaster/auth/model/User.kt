package uz.murodjon.filemaster.auth.model

import jakarta.persistence.*
import uz.murodjon.filemaster.auth.enums.UserPlan
import uz.murodjon.filemaster.files.model.StoredFile
import java.time.Instant

@Entity
@Table(name = "users")
class User(
    var guest: Boolean = true,
    var name: String? = null,
    @Column(unique = true) var email: String? = null,
    @OneToOne
    var avatar: StoredFile? = null,
    @Enumerated(EnumType.STRING)
    var plan: UserPlan = UserPlan.FREE,
    /** BCrypt hash; null for guests / not-yet-registered users. */
    var passwordHash: String? = null,
    /** Google `sub` once the account is linked via Google Sign-In; null otherwise. */
    @Column(unique = true) var googleId: String? = null,
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    var active: Boolean = true
    var createdTimestamp: Long? = Instant.now().epochSecond
    var updatedTimestamp: Long? = Instant.now().epochSecond
    var deletedTimestamp: Long? = null
}

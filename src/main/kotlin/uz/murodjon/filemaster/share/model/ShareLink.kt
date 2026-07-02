package uz.murodjon.filemaster.share.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import uz.murodjon.filemaster.auth.model.User
import uz.murodjon.filemaster.files.model.StoredFile
import java.time.Instant

@Entity
@Table(name = "share_links")
class ShareLink(
    @Column(unique = true, length = 128) var token: String,
    @ManyToOne(fetch = FetchType.LAZY) var file: StoredFile,
    @ManyToOne(fetch = FetchType.LAZY) var user: User,
    var expiresTimestamp: Long,
    var active: Boolean = true,
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    var createdTimestamp: Long? = Instant.now().epochSecond
}

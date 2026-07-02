package uz.murodjon.filemaster.auth.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

@Entity
@Table(name = "sessions")
class Session(
    @Column(unique = true, length = 128) var token: String,
    @ManyToOne(fetch = FetchType.LAZY) var user: User,
    var expiresTimestamp: Long,
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null
}

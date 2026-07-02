package uz.murodjon.filemaster.files.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import uz.murodjon.filemaster.auth.model.User
import uz.murodjon.filemaster.common.CategoryToken
import uz.murodjon.filemaster.files.enums.FileSource
import java.time.Instant
import java.util.UUID

/**
 * Pure file metadata — both user uploads and conversion results. Holds ONLY facts
 * about the file itself (name, size, format, where the bytes live), never anything
 * about the job that produced it. Job concerns (status, jobId, retention/expiry)
 * live on the conversion side, not here.
 *
 * The [source] discriminates upload vs result; [folder] is the storage sub-directory
 * (`image/`, `documents/`, `audio/`, ...) derived from [category]. In object storage the
 * file is named by its [uuid] (`<folder>/<uuid>.<ext>`), never by [originalName].
 */
@Entity
@Table(name = "files")
class StoredFile(
    @ManyToOne(fetch = FetchType.LAZY) var user: User,
    @Column(length = 1024) var originalName: String,
    @Column(length = 1024) var name: String,
    var bytes: Long,
    @Enumerated(EnumType.STRING) @Column(length = 16) var category: CategoryToken,
    @Enumerated(EnumType.STRING) @Column(length = 16) var source: FileSource,
    var folder: String,
    @Column(length = 1024) var absolutePath: String,
    @Column(length = 36) var uuid: UUID,
    @Column(length = 16) var format: String,
    var contentType: String? = null,
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    var active: Boolean = true
    var starred: Boolean = false
    var createdTimestamp: Long? = Instant.now().epochSecond
    var updatedTimestamp: Long? = Instant.now().epochSecond
    var deletedTimestamp: Long? = null
}

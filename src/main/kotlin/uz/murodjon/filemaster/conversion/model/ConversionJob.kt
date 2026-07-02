package uz.murodjon.filemaster.conversion.model

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Inheritance
import jakarta.persistence.InheritanceType
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import uz.murodjon.filemaster.auth.model.User
import uz.murodjon.filemaster.common.JobStatus
import uz.murodjon.filemaster.common.Quality
import uz.murodjon.filemaster.conversion.engine.ConversionSettings
import uz.murodjon.filemaster.tools.enums.ToolSlug
import java.time.Instant

/**
 * Base conversion job: holds only the fields every category shares. Category-specific knobs
 * (audio/video/image) live on the per-feature subclasses ([uz.murodjon.filemaster.audio.model.AudioConversionJob]
 * etc.), each in its own table via [InheritanceType.JOINED]. Document and archive jobs have no
 * extra knobs and use this base type directly.
 */
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "conversion_jobs")
open class ConversionJob(
    @ManyToOne(fetch = FetchType.LAZY) var user: User,
    var tool: ToolSlug,
    var outputFormat: String,
    var quality: Quality = Quality.BALANCED,
    var keepOriginal: Boolean = true,
    var mergeIntoOne: Boolean = false,
    @Enumerated(EnumType.STRING) @Column(length = 16) var status: JobStatus = JobStatus.QUEUED,
    var progress: Int = 0,

    @OneToMany(mappedBy = "job", cascade = [CascadeType.ALL], fetch = FetchType.EAGER, orphanRemoval = true)
    var files: MutableList<JobFile> = mutableListOf(),
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    var createdTimestamp: Long? = Instant.now().epochSecond
    var updatedTimestamp: Long? = Instant.now().epochSecond

    /**
     * Maps this job into engine [ConversionSettings]. The base only carries [quality]; each media
     * subclass overrides this to fold in its own knobs, so the worker never type-checks the job.
     */
    open fun toSettings(): ConversionSettings = ConversionSettings(quality = quality)
}

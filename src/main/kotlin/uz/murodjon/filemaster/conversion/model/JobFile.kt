package uz.murodjon.filemaster.conversion.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import uz.murodjon.filemaster.common.JobStatus
import uz.murodjon.filemaster.files.model.StoredFile

@Entity
@Table(name = "job_files")
class JobFile(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id")
    var job: ConversionJob? = null,
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "upload_id")
    var upload: StoredFile,
    var name: String,
    @Enumerated(EnumType.STRING) @Column(length = 16) var status: JobStatus = JobStatus.QUEUED,
    var progress: Int = 0,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "result_id")
    var result: StoredFile? = null,
    var bytes: Long? = null,
    @Column(length = 512) var error: String? = null,
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null
}

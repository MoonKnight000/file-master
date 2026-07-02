package uz.murodjon.filemaster.conversion.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import uz.murodjon.filemaster.common.JobStatus
import uz.murodjon.filemaster.conversion.model.ConversionJob
import java.util.Optional

@Repository
interface ConversionJobRepository : JpaRepository<ConversionJob, Long> {
    fun findByIdAndUserId(id: Long, userId: Long): Optional<ConversionJob>

    /** How many conversion jobs a user has started (account usage). */
    fun countByUserId(userId: Long): Long

    /** Filtered by status list with dynamic sort from Pageable. */
    fun findByUserIdAndStatusIn(userId: Long, statuses: List<JobStatus>, pageable: Pageable): Page<ConversionJob>
}

package uz.murodjon.filemaster.files.repository

import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import uz.murodjon.filemaster.auth.enums.UserPlan
import uz.murodjon.filemaster.common.JobStatus
import uz.murodjon.filemaster.files.enums.FileSource
import uz.murodjon.filemaster.files.model.StoredFile
import java.util.Optional

@Repository
interface StoredFileRepository : JpaRepository<StoredFile, Long>, JpaSpecificationExecutor<StoredFile> {

    fun findByIdAndUserIdAndActiveTrue(id: Long, userId: Long): Optional<StoredFile>

    fun countByUserIdAndActiveTrue(userId: Long): Long

    /** All of a user's active files (no paging) — used by account deletion to purge storage. */
    fun findByUserIdAndActiveTrue(userId: Long): List<StoredFile>

    /** Total size of a user's active files (0 when they have none). */
    @Query("select coalesce(sum(f.bytes), 0) from StoredFile f where f.user.id = :userId and f.active = true")
    fun sumActiveBytes(userId: Long): Long

    /** Per-category active-file counts for the account usage breakdown. */
    @Query(
        "select f.category as category, count(f) as count from StoredFile f " +
            "where f.user.id = :userId and f.active = true group by f.category",
    )
    fun countActiveByCategory(userId: Long): List<CategoryCountView>

    fun findByUserIdAndActiveTrueAndOriginalNameContainingIgnoreCase(
        userId: Long,
        q: String,
        pageable: Pageable,
    ): List<StoredFile>

    /** Active files of a given source created before [cutoff] — used by retention cleanup. */
    fun findBySourceAndActiveTrueAndCreatedTimestampLessThan(
        source: FileSource,
        cutoff: Long,
    ): List<StoredFile>

    /** Same as above but only for owners on [plan] — retention windows differ per plan. */
    @Query(
        "select f from StoredFile f where f.source = :source and f.active = true " +
            "and f.createdTimestamp < :cutoff and f.user.plan = :plan",
    )
    fun findExpiredResultsForPlan(
        source: FileSource,
        cutoff: Long,
        plan: UserPlan,
    ): List<StoredFile>

    /**
     * Expired files of [source] for owners on [plan] that no unfinished job still needs —
     * used to sweep old UPLOADs without pulling inputs out from under a queued/processing job.
     */
    @Query(
        "select f from StoredFile f where f.source = :source and f.active = true " +
            "and f.createdTimestamp < :cutoff and f.user.plan = :plan " +
            "and not exists (select jf from JobFile jf where jf.upload = f and jf.job.status in :busyStatuses)",
    )
    fun findExpiredUnreferencedForPlan(
        source: FileSource,
        cutoff: Long,
        plan: UserPlan,
        busyStatuses: List<JobStatus>,
    ): List<StoredFile>
}

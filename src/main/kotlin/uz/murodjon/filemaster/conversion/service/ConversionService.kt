package uz.murodjon.filemaster.conversion.service

import uz.murodjon.filemaster.auth.model.User
import uz.murodjon.filemaster.conversion.dto.ConversionFilterRequest
import uz.murodjon.filemaster.conversion.dto.JobDto
import uz.murodjon.filemaster.conversion.model.ConversionJob
import uz.murodjon.filemaster.files.model.StoredFile
import uz.murodjon.filemaster.util.PageableData
import java.io.OutputStream

interface ConversionService {
    /**
     * Persists an already-built [job] (the per-category services construct the right
     * [ConversionJob] subtype and do their own validation), attaches the [sources] as job files
     * and triggers the worker once the transaction commits. This only persists and kicks off
     * processing — no category-specific logic lives here.
     */
    fun submit(job: ConversionJob, sources: List<StoredFile>): JobDto

    fun status(user: User, jobId: Long): JobDto

    /** Filtered, paginated job history for the current user. */
    fun filter(user: User, filter: ConversionFilterRequest): PageableData<JobDto>

    /** Writes a zip of all of a job's results to [out]. */
    fun writeResultsZip(user: User, jobId: Long, out: OutputStream)
}

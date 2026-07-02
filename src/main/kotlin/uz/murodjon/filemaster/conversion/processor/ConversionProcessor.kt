package uz.murodjon.filemaster.conversion.processor

import uz.murodjon.filemaster.conversion.model.ConversionJob
import uz.murodjon.filemaster.tools.dto.ToolDef
import uz.murodjon.filemaster.tools.enums.ToolGroup
import java.nio.file.Path

/**
 * Runs the actual conversion for one file-type [group] (audio / video / image / document /
 * archive). Each implementation owns its own engines and quirks, so video and PDF logic no
 * longer share a single class. The worker only routes a job to the matching processor; the
 * common plumbing (job state, progress events, result storage) lives in [ConversionSupport].
 */
interface ConversionProcessor {

    /** The file-type group this processor handles. The worker dispatches by this. */
    val group: ToolGroup

    /**
     * Converts the job's files. [workDir] is a fresh, per-job scratch directory the worker
     * creates and deletes; the implementation is responsible for moving the job to its final
     * status and emitting the terminal `done` event (usually via [ConversionSupport]).
     */
    fun process(job: ConversionJob, tool: ToolDef, workDir: Path)
}

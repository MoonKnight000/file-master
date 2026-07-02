package uz.murodjon.filemaster.conversion.processor

import org.springframework.stereotype.Component
import uz.murodjon.filemaster.conversion.engine.FfmpegConverter
import uz.murodjon.filemaster.conversion.engine.FfmpegMerger
import uz.murodjon.filemaster.conversion.model.ConversionJob
import uz.murodjon.filemaster.tools.dto.ToolDef
import uz.murodjon.filemaster.tools.enums.ToolGroup
import uz.murodjon.filemaster.tools.enums.ToolKind
import java.nio.file.Path

/** Video jobs: ffmpeg 1->1 conversions/edits (incl. video-to-gif), plus the n->1 merge-videos concat. */
@Component
class VideoProcessor(
    private val ffmpeg: FfmpegConverter,
    private val merger: FfmpegMerger,
    private val support: ConversionSupport,
) : ConversionProcessor {

    override val group = ToolGroup.VIDEO

    override fun process(job: ConversionJob, tool: ToolDef, workDir: Path) {
        if (tool.kind == ToolKind.MERGE) {
            support.combineAll(job, workDir) { inputs -> merger.mergeVideos(inputs, job.outputFormat, workDir) }
        } else {
            support.convertEachFile(job, workDir, ffmpeg)
        }
    }
}

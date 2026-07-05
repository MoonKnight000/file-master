package uz.murodjon.filemaster.conversion.processor

import org.springframework.stereotype.Component
import uz.murodjon.filemaster.conversion.engine.FfmpegConverter
import uz.murodjon.filemaster.conversion.engine.FfmpegMerger
import uz.murodjon.filemaster.conversion.engine.WhisperConverter
import uz.murodjon.filemaster.conversion.model.ConversionJob
import uz.murodjon.filemaster.tools.dto.ToolDef
import uz.murodjon.filemaster.tools.enums.ToolEngine
import uz.murodjon.filemaster.tools.enums.ToolGroup
import uz.murodjon.filemaster.tools.enums.ToolKind
import java.nio.file.Path

/**
 * Audio jobs: ffmpeg 1->1 conversions/edits, the n->1 merge-audio concat, and the
 * whisper.cpp transcription (audio-to-text).
 */
@Component
class AudioProcessor(
    private val ffmpeg: FfmpegConverter,
    private val merger: FfmpegMerger,
    private val whisper: WhisperConverter,
    private val support: ConversionSupport,
) : ConversionProcessor {

    override val group = ToolGroup.AUDIO

    override fun process(job: ConversionJob, tool: ToolDef, workDir: Path) {
        when {
            tool.kind == ToolKind.MERGE ->
                support.combineAll(job, workDir) { inputs -> merger.mergeAudio(inputs, job.outputFormat, workDir) }
            tool.engine == ToolEngine.TRANSCRIBE -> support.convertEachFile(job, workDir, whisper)
            else -> support.convertEachFile(job, workDir, ffmpeg)
        }
    }
}

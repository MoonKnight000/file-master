package uz.murodjon.filemaster.conversion.processor

import org.springframework.stereotype.Component
import uz.murodjon.filemaster.conversion.engine.FfmpegConverter
import uz.murodjon.filemaster.conversion.model.ConversionJob
import uz.murodjon.filemaster.tools.dto.ToolDef
import uz.murodjon.filemaster.tools.enums.ToolGroup
import java.nio.file.Path

/** Audio jobs (convert-audio): always ffmpeg, one output per input. */
@Component
class AudioProcessor(
    private val ffmpeg: FfmpegConverter,
    private val support: ConversionSupport,
) : ConversionProcessor {

    override val group = ToolGroup.AUDIO

    override fun process(job: ConversionJob, tool: ToolDef, workDir: Path) =
        support.convertEachFile(job, workDir, ffmpeg)
}

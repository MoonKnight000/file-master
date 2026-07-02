package uz.murodjon.filemaster.conversion.processor

import org.springframework.stereotype.Component
import uz.murodjon.filemaster.conversion.engine.FfmpegConverter
import uz.murodjon.filemaster.conversion.engine.ImageConverter
import uz.murodjon.filemaster.conversion.model.ConversionJob
import uz.murodjon.filemaster.tools.dto.ToolDef
import uz.murodjon.filemaster.tools.enums.ToolEngine
import uz.murodjon.filemaster.tools.enums.ToolGroup
import java.nio.file.Path

/**
 * Image jobs, one output per input. compress-image uses the pure-Java ImageIO converter;
 * convert-image is delegated to ffmpeg (it covers webp/heic/etc. that ImageIO can't).
 */
@Component
class ImageProcessor(
    private val image: ImageConverter,
    private val ffmpeg: FfmpegConverter,
    private val support: ConversionSupport,
) : ConversionProcessor {

    override val group = ToolGroup.IMAGE

    override fun process(job: ConversionJob, tool: ToolDef, workDir: Path) {
        val converter = if (tool.engine == ToolEngine.IMAGE) image else ffmpeg
        support.convertEachFile(job, workDir, converter)
    }
}

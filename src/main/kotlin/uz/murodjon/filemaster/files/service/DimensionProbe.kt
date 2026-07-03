package uz.murodjon.filemaster.files.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import uz.murodjon.filemaster.common.CategoryToken
import uz.murodjon.filemaster.config.AppProperties
import uz.murodjon.filemaster.conversion.engine.FfmpegConverter
import uz.murodjon.filemaster.conversion.engine.ProcessRunner
import java.nio.file.Path
import javax.imageio.ImageIO

/**
 * Best-effort pixel-dimension probe run at upload time: images via an ImageIO header read
 * (no full decode), videos via ffprobe. Never throws — a file the probe can't read simply
 * keeps null dimensions and crop validation falls back to the job-level failure.
 */
@Component
class DimensionProbe(
    private val runner: ProcessRunner,
    private val props: AppProperties,
) {

    private val log = LoggerFactory.getLogger(javaClass)

    fun probe(file: Path, category: CategoryToken): Pair<Int, Int>? = runCatching {
        when (category) {
            CategoryToken.IMAGE -> probeImage(file)
            CategoryToken.VIDEO -> probeVideo(file)
            else -> null
        }
    }.onFailure { log.debug("Dimension probe failed for {}: {}", file, it.message) }.getOrNull()

    private fun probeImage(file: Path): Pair<Int, Int>? =
        ImageIO.createImageInputStream(file.toFile())?.use { stream ->
            val readers = ImageIO.getImageReaders(stream)
            if (!readers.hasNext()) return null
            val reader = readers.next()
            try {
                reader.input = stream
                reader.getWidth(0) to reader.getHeight(0)
            } finally {
                reader.dispose()
            }
        }

    /** First video stream's width×height (same ffprobe invocation as FfmpegMerger). */
    private fun probeVideo(file: Path): Pair<Int, Int>? {
        val out = runner.run(
            listOf(
                FfmpegConverter.ffprobePath(props.tools.ffmpegPath), "-v", "error", "-select_streams", "v:0",
                "-show_entries", "stream=width,height", "-of", "csv=s=x:p=0", file.toString(),
            ),
            30,
        ).trim()
        val parts = out.lines().firstOrNull()?.split("x") ?: return null
        val w = parts.getOrNull(0)?.trim()?.toIntOrNull() ?: return null
        val h = parts.getOrNull(1)?.trim()?.toIntOrNull() ?: return null
        return w to h
    }
}

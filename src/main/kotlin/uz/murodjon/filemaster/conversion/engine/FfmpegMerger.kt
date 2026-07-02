package uz.murodjon.filemaster.conversion.engine

import org.springframework.stereotype.Component
import uz.murodjon.filemaster.config.AppProperties
import uz.murodjon.filemaster.exception.ConversionFailedException
import java.nio.file.Files
import java.nio.file.Path

/**
 * Joins several audio or video files into one via the ffmpeg concat filter (re-encodes, so the
 * inputs may differ in codec/container; videos are letterboxed to the first input's frame size).
 */
@Component
class FfmpegMerger(
    private val props: AppProperties,
    private val runner: ProcessRunner,
) {

    /** Concatenates [inputs] (2+) into one audio file `merged.<ext>` in [workDir]. */
    fun mergeAudio(inputs: List<Path>, outputFormat: String, workDir: Path): Path {
        require(inputs.size >= 2) { "Merging needs at least two inputs." }
        val output = workDir.resolve("merged.${outputFormat.lowercase()}")
        val command = mutableListOf(props.tools.ffmpegPath, "-y")
        inputs.forEach { command += listOf("-i", it.toString()) }
        val pads = inputs.indices.joinToString("") { "[$it:a]" }
        command += listOf(
            "-filter_complex", "${pads}concat=n=${inputs.size}:v=0:a=1[a]",
            "-map", "[a]",
            output.toString(),
        )
        runner.run(command, props.tools.timeoutSeconds)
        if (!Files.exists(output)) throw ConversionFailedException("ffmpeg did not produce the merged audio.")
        return output
    }

    /**
     * Concatenates [inputs] (2+) into one video `merged.<ext>` in [workDir]. Every input is
     * scaled/letterboxed to the first input's dimensions; audio is kept only when every input
     * has an audio stream (otherwise the result is silent).
     */
    fun mergeVideos(inputs: List<Path>, outputFormat: String, workDir: Path): Path {
        require(inputs.size >= 2) { "Merging needs at least two inputs." }
        val output = workDir.resolve("merged.${outputFormat.lowercase()}")
        val (width, height) = probeDimensions(inputs.first())
        val withAudio = inputs.all { hasAudioStream(it) }

        val command = mutableListOf(props.tools.ffmpegPath, "-y")
        inputs.forEach { command += listOf("-i", it.toString()) }

        val filter = buildString {
            inputs.indices.forEach { i ->
                append("[$i:v]scale=$width:$height:force_original_aspect_ratio=decrease,")
                append("pad=$width:$height:(ow-iw)/2:(oh-ih)/2,setsar=1,fps=30[v$i];")
            }
            inputs.indices.forEach { i ->
                append("[v$i]")
                if (withAudio) append("[$i:a]")
            }
            append("concat=n=${inputs.size}:v=1:a=${if (withAudio) 1 else 0}[v]")
            if (withAudio) append("[a]")
        }
        command += listOf("-filter_complex", filter, "-map", "[v]")
        command += if (withAudio) listOf("-map", "[a]") else listOf("-an")
        command += output.toString()

        runner.run(command, props.tools.timeoutSeconds)
        if (!Files.exists(output)) throw ConversionFailedException("ffmpeg did not produce the merged video.")
        return output
    }

    /** First video stream's width×height via ffprobe. */
    private fun probeDimensions(input: Path): Pair<Int, Int> {
        val out = runner.run(
            listOf(ffprobePath(), "-v", "error", "-select_streams", "v:0",
                "-show_entries", "stream=width,height", "-of", "csv=s=x:p=0", input.toString()),
            30,
        ).trim()
        val parts = out.lines().first().split("x")
        val w = parts.getOrNull(0)?.trim()?.toIntOrNull()
        val h = parts.getOrNull(1)?.trim()?.toIntOrNull()
        if (w == null || h == null) throw ConversionFailedException("Could not read the video dimensions.")
        // Even dimensions keep every encoder happy.
        return (w - w % 2) to (h - h % 2)
    }

    private fun hasAudioStream(input: Path): Boolean = runCatching {
        runner.run(
            listOf(ffprobePath(), "-v", "error", "-select_streams", "a",
                "-show_entries", "stream=index", "-of", "csv=p=0", input.toString()),
            30,
        ).isNotBlank()
    }.getOrDefault(false)

    private fun ffprobePath(): String = FfmpegConverter.ffprobePath(props.tools.ffmpegPath)
}

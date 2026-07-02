package uz.murodjon.filemaster.conversion.engine

import org.springframework.stereotype.Component
import uz.murodjon.filemaster.common.Quality
import uz.murodjon.filemaster.config.AppProperties
import uz.murodjon.filemaster.exception.ConversionFailedException
import java.nio.file.Files
import java.nio.file.Path

/** Audio / video / image conversions via the ffmpeg CLI. */
@Component
class FfmpegConverter(
    private val props: AppProperties,
    private val runner: ProcessRunner,
) : Converter {

    private val audioExts = setOf("mp3", "wav", "flac", "aac", "m4a", "ogg", "wma")
    private val videoExts = setOf("mp4", "mov", "webm", "mkv", "avi")
    private val imageExts = setOf("png", "jpg", "jpeg", "webp", "bmp", "gif", "tiff")

    private fun defaultBitrateKbps(quality: Quality): Int = when (quality) {
        Quality.HIGH -> 320
        Quality.BALANCED -> 192
        Quality.SMALL -> 96
    }

    private fun codecArg(codec: String): String = when (codec.lowercase()) {
        "h264" -> "libx264"
        "h265", "hevc" -> "libx265"
        "vp9" -> "libvpx-vp9"
        else -> codec
    }

    override fun convert(input: Path, outputFormat: String, settings: ConversionSettings, workDir: Path): Path {
        val targetExt = outputFormat.lowercase()
        val baseName = input.fileName.toString().substringBeforeLast('.')
        // Write to a sub-directory so the output never collides with the input path: ffmpeg refuses
        // to use the same file as both in and out (exit -22), which happens for same-format tools
        // like compress-video / trim-video (mp4 -> mp4). Keeps the clean "<base>.<ext>" result name.
        val outDir = Files.createDirectories(workDir.resolve("out"))
        val output = outDir.resolve("$baseName.$targetExt")

        val command = mutableListOf(props.tools.ffmpegPath, "-y")

        // Trim: seek before -i (fast), then cap the duration.
        settings.trimStartSeconds?.takeIf { it > 0 }?.let { command += listOf("-ss", trimValue(it)) }
        command += listOf("-i", input.toString())
        val duration = settings.trimEndSeconds?.let { end -> end - (settings.trimStartSeconds ?: 0.0) }
        if (duration != null && duration > 0) command += listOf("-t", trimValue(duration))

        when {
            targetExt in audioExts -> applyAudio(command, settings, withDefaultBitrate = true)
            targetExt in videoExts -> {
                applyVideo(command, settings)
                if (settings.muteAudio) {
                    command += "-an"
                } else {
                    applyAudio(command, settings, withDefaultBitrate = false) // the video's audio track
                }
            }
            targetExt in imageExts -> applyImage(command, settings, targetExt)
            // anything else: no extra args
        }

        command += output.toString()
        runner.run(command, props.tools.timeoutSeconds)
        if (!Files.exists(output)) {
            throw ConversionFailedException("ffmpeg did not produce $baseName.$targetExt")
        }
        return output
    }

    private fun applyAudio(command: MutableList<String>, settings: ConversionSettings, withDefaultBitrate: Boolean) {
        val bitrate = settings.audioBitrateKbps ?: if (withDefaultBitrate) defaultBitrateKbps(settings.quality) else null
        bitrate?.let { command += listOf("-b:a", "${it}k") }
        settings.audioSampleRate?.let { command += listOf("-ar", it.toString()) }
        settings.audioChannels?.let { command += listOf("-ac", it.toString()) }
        // Build audio filter chain: volume and/or loudnorm.
        val filters = buildList {
            settings.audioVolume?.takeIf { it != 1.0 }?.let { add("volume=$it") }
            if (settings.audioNormalize) add("loudnorm")
        }
        if (filters.isNotEmpty()) command += listOf("-af", filters.joinToString(","))
    }

    private fun applyVideo(command: MutableList<String>, settings: ConversionSettings) {
        settings.videoCodec?.let { command += listOf("-c:v", codecArg(it)) }
        settings.videoBitrateKbps?.let { command += listOf("-b:v", "${it}k") }
        // Scale to the requested height, keeping aspect ratio (width rounded to an even number).
        settings.videoResolution?.let { res ->
            heightOf(res)?.let { h -> command += listOf("-vf", "scale=-2:$h") }
        }
        settings.videoFps?.let { command += listOf("-r", it.toString()) }
    }

    private fun applyImage(command: MutableList<String>, settings: ConversionSettings, targetExt: String) {
        val w = settings.imageWidth
        val h = settings.imageHeight
        // Resize keeping aspect: -2 = auto (even) for the missing dimension; both = fit within.
        when {
            w != null && h != null -> command += listOf("-vf", "scale=$w:$h:force_original_aspect_ratio=decrease")
            w != null -> command += listOf("-vf", "scale=$w:-1")
            h != null -> command += listOf("-vf", "scale=-1:$h")
        }
        // JPEG/WebP quality (1..100 -> ffmpeg qscale 31..2; lower qscale = better).
        settings.imageQuality?.let { q ->
            if (targetExt == "jpg" || targetExt == "jpeg") {
                val qscale = (31 - (q.coerceIn(1, 100) - 1) * 29 / 99).coerceIn(2, 31)
                command += listOf("-q:v", qscale.toString())
            } else if (targetExt == "webp") {
                command += listOf("-quality", q.coerceIn(1, 100).toString())
            }
        }
    }

    private fun heightOf(resolution: String): Int? =
        resolution.trim().removeSuffix("p").toIntOrNull()

    /** ffmpeg accepts plain seconds; trim whitespace and avoid scientific notation. */
    private fun trimValue(seconds: Double): String =
        if (seconds == seconds.toLong().toDouble()) seconds.toLong().toString() else seconds.toString()
}

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
            targetExt == "gif" -> applyGif(command, settings)
            targetExt in audioExts -> applyAudio(command, settings, withDefaultBitrate = true, input = input)
            targetExt in videoExts -> {
                applyVideo(command, settings)
                if (settings.muteAudio) {
                    command += "-an"
                } else {
                    applyAudio(command, settings, withDefaultBitrate = false, input = input) // the video's audio track
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

    private fun applyAudio(command: MutableList<String>, settings: ConversionSettings, withDefaultBitrate: Boolean, input: Path) {
        val bitrate = settings.audioBitrateKbps ?: if (withDefaultBitrate) defaultBitrateKbps(settings.quality) else null
        bitrate?.let { command += listOf("-b:a", "${it}k") }
        settings.audioSampleRate?.let { command += listOf("-ar", it.toString()) }
        settings.audioChannels?.let { command += listOf("-ac", it.toString()) }
        // Audio filter chain: volume → reverse → fades → tempo → loudnorm.
        val filters = buildList {
            settings.audioVolume?.takeIf { it != 1.0 }?.let { add("volume=$it") }
            if (settings.reverseAudio) add("areverse")
            settings.fadeInSeconds?.takeIf { it > 0 }?.let { add("afade=t=in:st=0:d=${trimValue(it)}") }
            settings.fadeOutSeconds?.takeIf { it > 0 }?.let { fade ->
                // afade t=out needs an absolute start time — derive it from the (trimmed) duration.
                effectiveDuration(input, settings)?.let { dur ->
                    val start = (dur - fade).coerceAtLeast(0.0)
                    add("afade=t=out:st=${trimValue(start)}:d=${trimValue(fade)}")
                }
            }
            settings.speedFactor?.takeIf { it != 1.0 }?.let { addAll(atempoChain(it)) }
            if (settings.audioNormalize) add("loudnorm")
        }
        if (filters.isNotEmpty()) command += listOf("-af", filters.joinToString(","))
    }

    private fun applyVideo(command: MutableList<String>, settings: ConversionSettings) {
        settings.videoCodec?.let { command += listOf("-c:v", codecArg(it)) }
        settings.videoBitrateKbps?.let { command += listOf("-b:v", "${it}k") }
        // Video filter chain: crop → rotate → flip → scale → speed → watermark.
        val filters = buildList {
            if (settings.cropWidth != null && settings.cropHeight != null) {
                add("crop=${settings.cropWidth}:${settings.cropHeight}:${settings.cropX ?: 0}:${settings.cropY ?: 0}")
            }
            when (((settings.rotateDegrees ?: 0) % 360 + 360) % 360) {
                90 -> add("transpose=1")
                180 -> { add("hflip"); add("vflip") }
                270 -> add("transpose=2")
            }
            when (settings.flipDirection) {
                "horizontal" -> add("hflip")
                "vertical" -> add("vflip")
            }
            settings.videoResolution?.let { res -> heightOf(res)?.let { h -> add("scale=-2:$h") } }
            settings.speedFactor?.takeIf { it != 1.0 }?.let { add("setpts=PTS/${trimValue(it)}") }
            settings.watermarkText?.takeIf { it.isNotBlank() }?.let { text ->
                add(drawtextFilter(text, settings.watermarkPosition ?: "bottom-right",
                    settings.watermarkOpacity ?: 0.5, settings.watermarkFontSize ?: 48))
            }
        }
        if (filters.isNotEmpty()) command += listOf("-vf", filters.joinToString(","))
        settings.videoFps?.let { command += listOf("-r", it.toString()) }
    }

    /** Animated GIF via the palettegen/paletteuse pair (much better colors than the default). */
    private fun applyGif(command: MutableList<String>, settings: ConversionSettings) {
        val fps = settings.videoFps ?: 12
        val height = settings.videoResolution?.let { heightOf(it) } ?: 480
        command += listOf(
            "-filter_complex",
            "[0:v]fps=$fps,scale=-2:$height:flags=lanczos,split[s0][s1];[s0]palettegen[p];[s1][p]paletteuse",
            "-loop", "0",
        )
    }

    /**
     * ffmpeg's atempo only accepts 0.5..2 per instance — chain instances to cover 0.25..4
     * (e.g. 4x = atempo=2,atempo=2; 0.25x = atempo=0.5,atempo=0.5).
     */
    private fun atempoChain(factor: Double): List<String> {
        val chain = mutableListOf<String>()
        var remaining = factor.coerceIn(0.25, 4.0)
        while (remaining > 2.0) { chain += "atempo=2.0"; remaining /= 2.0 }
        while (remaining < 0.5) { chain += "atempo=0.5"; remaining /= 0.5 }
        if (remaining != 1.0) chain += "atempo=${trimValue(remaining)}"
        return chain
    }

    /** drawtext overlay. "diagonal" (a PDF/image position) falls back to center — drawtext cannot rotate. */
    private fun drawtextFilter(text: String, position: String, opacity: Double, fontSize: Int): String {
        // Keep the filter spec parseable: strip the characters drawtext treats specially.
        val safe = text.replace(Regex("""[\\'":;%,\[\]=]"""), " ").trim()
        val (x, y) = when (position) {
            "top-left" -> "24" to "24"
            "top-right" -> "w-tw-24" to "24"
            "bottom-left" -> "24" to "h-th-24"
            "bottom-right" -> "w-tw-24" to "h-th-24"
            else -> "(w-tw)/2" to "(h-th)/2" // center / diagonal
        }
        val alpha = opacity.coerceIn(0.0, 1.0)
        // font= resolves through fontconfig — a fontfile= path would need filter-level colon escaping.
        return "drawtext=font=Arial:text=$safe:x=$x:y=$y" +
            ":fontsize=$fontSize:fontcolor=white@$alpha:borderw=2:bordercolor=black@${trimValue(alpha * 0.6)}"
    }

    /** The output duration after trimming (null when ffprobe can't tell — fade-out is skipped then). */
    private fun effectiveDuration(input: Path, settings: ConversionSettings): Double? {
        val total = probeDurationSeconds(input) ?: return null
        val start = (settings.trimStartSeconds ?: 0.0).coerceIn(0.0, total)
        val end = (settings.trimEndSeconds ?: total).coerceIn(start, total)
        return end - start
    }

    /** Media duration in seconds via ffprobe (sits next to ffmpeg). Null on any failure. */
    private fun probeDurationSeconds(input: Path): Double? = runCatching {
        runner.run(
            listOf(ffprobePath(props.tools.ffmpegPath), "-v", "error",
                "-show_entries", "format=duration", "-of", "csv=p=0", input.toString()),
            30,
        ).trim().toDouble()
    }.getOrNull()

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

    companion object {
        /**
         * ffprobe sits next to ffmpeg — swap only the FILE NAME. A plain `path.replace("ffmpeg",
         * "ffprobe")` would also rewrite a directory named ffmpeg (e.g. `C:/Program Files/ffmpeg/bin/`).
         */
        fun ffprobePath(ffmpegPath: String): String {
            val p = java.nio.file.Paths.get(ffmpegPath)
            return (p.parent?.resolve(p.fileName.toString().replace("ffmpeg", "ffprobe")) ?: p).toString()
        }
    }
}

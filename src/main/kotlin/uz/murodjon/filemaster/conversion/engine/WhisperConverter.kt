package uz.murodjon.filemaster.conversion.engine

import org.springframework.stereotype.Component
import uz.murodjon.filemaster.config.AppProperties
import uz.murodjon.filemaster.exception.ConversionFailedException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

/**
 * Speech-to-text via the whisper.cpp CLI (`whisper-cli`), producing TXT, SRT or VTT.
 * Whisper only ingests 16 kHz mono WAV, so the input is first converted with ffmpeg.
 * Binary + ggml model paths: `app.tools.whisper-path` / `whisper-model-path`; language
 * comes from the job (`auto` = let the model detect it). CPU transcription is slow, so
 * it runs under its own `whisper-timeout-seconds` instead of the shared tool timeout.
 */
@Component
class WhisperConverter(
    private val props: AppProperties,
    private val runner: ProcessRunner,
) : Converter {

    override fun convert(input: Path, outputFormat: String, settings: ConversionSettings, workDir: Path): Path {
        if (!Files.exists(Paths.get(props.tools.whisperPath)) || !Files.exists(Paths.get(props.tools.whisperModelPath))) {
            throw ConversionFailedException("Speech-to-text engine is not installed on the server.")
        }

        // 1) Any audio → 16 kHz mono PCM WAV (the only input whisper.cpp accepts).
        val wav = workDir.resolve("whisper-input.wav")
        runner.run(
            listOf(
                props.tools.ffmpegPath, "-y", "-i", input.toString(),
                "-ar", "16000", "-ac", "1", "-c:a", "pcm_s16le", wav.toString(),
            ),
            props.tools.timeoutSeconds,
        )

        // 2) Transcribe. -of sets the output base; -otxt/-osrt/-ovtt appends the extension.
        val ext = outputFormat.lowercase().let { if (it in listOf("txt", "srt", "vtt")) it else "txt" }
        val outBase = workDir.resolve(input.fileName.toString().substringBeforeLast('.'))
        val cmd = mutableListOf(
            props.tools.whisperPath,
            "-m", props.tools.whisperModelPath,
            "-f", wav.toString(),
            "-of", outBase.toString(),
            "-o$ext",
            "-np", // no progress spam on stdout
        )
        settings.transcribeLanguage?.takeIf { it.isNotBlank() }?.let { cmd += listOf("-l", it) }
        runner.run(cmd, props.tools.whisperTimeoutSeconds)

        val output = workDir.resolve("${outBase.fileName}.$ext")
        if (!Files.exists(output)) throw ConversionFailedException("Whisper produced no transcript.")
        return output
    }
}

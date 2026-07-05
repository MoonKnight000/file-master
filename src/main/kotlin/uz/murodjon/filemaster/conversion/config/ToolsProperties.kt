package uz.murodjon.filemaster.conversion.config

data class ToolsProperties(
    val sofficePath: String,
    val ffmpegPath: String,
    val gsPath: String,
    val tesseractPath: String,
    val djvuPath: String,
    val workDir: String,
    val timeoutSeconds: Long,
    /** u2net ONNX model for remove-background (runs in-JVM via ONNX Runtime). */
    val u2netModelPath: String,
    /** whisper.cpp CLI + ggml model for audio-to-text. */
    val whisperPath: String,
    val whisperModelPath: String,
    /** Transcription is much slower than other conversions, so it gets its own timeout. */
    val whisperTimeoutSeconds: Long = 900,
)

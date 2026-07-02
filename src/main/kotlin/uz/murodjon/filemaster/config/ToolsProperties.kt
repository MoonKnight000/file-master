package uz.murodjon.filemaster.config

data class ToolsProperties(
    val sofficePath: String,
    val ffmpegPath: String,
    val gsPath: String,
    val tesseractPath: String,
    val djvuPath: String,
    val workDir: String,
    val timeoutSeconds: Long,
)

package uz.murodjon.filemaster.conversion.engine

import org.springframework.stereotype.Component
import uz.murodjon.filemaster.config.AppProperties
import uz.murodjon.filemaster.exception.ConversionFailedException
import java.nio.file.Files
import java.nio.file.Path

/**
 * OCR via the Tesseract CLI:
 *   tesseract <input-image> <out-base> <txt|pdf>
 * Tesseract appends the extension to the out-base. Requires Tesseract installed
 * (`tesseract`) — see `app.tools.tesseract-path`. Works on images; PDF inputs would
 * first need rasterizing (not handled here).
 */
@Component
class TesseractConverter(
    private val props: AppProperties,
    private val runner: ProcessRunner,
) : Converter {

    override fun convert(input: Path, outputFormat: String, settings: ConversionSettings, workDir: Path): Path {
        val targetExt = if (outputFormat.equals("pdf", ignoreCase = true)) "pdf" else "txt"
        val baseName = input.fileName.toString().substringBeforeLast('.')
        val outBase = workDir.resolve(baseName)

        val cmd = mutableListOf(props.tools.tesseractPath, input.toString(), outBase.toString())
        settings.ocrLanguage?.takeIf { it.isNotBlank() }?.let { cmd += listOf("-l", it) }
        cmd += targetExt
        runner.run(cmd, props.tools.timeoutSeconds)

        val output = workDir.resolve("$baseName.$targetExt")
        if (!Files.exists(output)) throw ConversionFailedException("Tesseract produced no file.")
        return output
    }
}

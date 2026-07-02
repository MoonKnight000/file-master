package uz.murodjon.filemaster.conversion.engine

import org.springframework.stereotype.Component
import uz.murodjon.filemaster.common.Quality
import uz.murodjon.filemaster.config.AppProperties
import uz.murodjon.filemaster.exception.ConversionFailedException
import java.nio.file.Files
import java.nio.file.Path

/**
 * PDF compression via the Ghostscript CLI:
 *   gs -sDEVICE=pdfwrite -dPDFSETTINGS=<preset> -o out.pdf in.pdf
 * Requires Ghostscript installed (`gs` / `gswin64c`) — see `app.tools.gs-path`.
 */
@Component
class GhostscriptConverter(
    private val props: AppProperties,
    private val runner: ProcessRunner,
) : Converter {

    /** Lower preset = smaller file / lower dpi. */
    private fun preset(quality: Quality): String = when (quality) {
        Quality.HIGH -> "/printer"   // ~300 dpi
        Quality.BALANCED -> "/ebook" // ~150 dpi
        Quality.SMALL -> "/screen"   // ~72 dpi
    }

    override fun convert(input: Path, outputFormat: String, settings: ConversionSettings, workDir: Path): Path {
        val baseName = input.fileName.toString().substringBeforeLast('.')
        val output = workDir.resolve("$baseName-compressed.pdf")

        runner.run(
            listOf(
                props.tools.gsPath,
                "-sDEVICE=pdfwrite",
                "-dCompatibilityLevel=1.4",
                "-dPDFSETTINGS=${preset(settings.quality)}",
                "-dNOPAUSE", "-dBATCH", "-dQUIET",
                "-sOutputFile=$output",
                input.toString(),
            ),
            props.tools.timeoutSeconds,
        )

        if (!Files.exists(output)) throw ConversionFailedException("Ghostscript produced no file.")
        return output
    }
}

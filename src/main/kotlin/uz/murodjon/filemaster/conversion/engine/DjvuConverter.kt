package uz.murodjon.filemaster.conversion.engine

import org.springframework.stereotype.Component
import uz.murodjon.filemaster.config.AppProperties
import uz.murodjon.filemaster.exception.ConversionFailedException
import java.nio.file.Files
import java.nio.file.Path

/**
 * DjVu → PDF via the DjVuLibre CLI:
 *   ddjvu -format=pdf input.djvu output.pdf
 * Requires DjVuLibre installed — see `app.tools.djvu-path`.
 */
@Component
class DjvuConverter(
    private val props: AppProperties,
    private val runner: ProcessRunner,
) : Converter {

    override fun convert(input: Path, outputFormat: String, settings: ConversionSettings, workDir: Path): Path {
        val baseName = input.fileName.toString().substringBeforeLast('.')
        val output = workDir.resolve("$baseName.pdf")

        runner.run(
            listOf(
                props.tools.djvuPath,
                "-format=pdf",
                input.toString(),
                output.toString(),
            ),
            props.tools.timeoutSeconds,
        )

        if (!Files.exists(output)) throw ConversionFailedException("ddjvu produced no file.")
        return output
    }
}

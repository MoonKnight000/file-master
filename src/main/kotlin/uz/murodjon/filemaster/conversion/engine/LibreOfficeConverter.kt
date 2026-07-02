package uz.murodjon.filemaster.conversion.engine

import org.springframework.stereotype.Component
import uz.murodjon.filemaster.config.AppProperties
import uz.murodjon.filemaster.exception.ConversionFailedException
import java.nio.file.Files
import java.nio.file.Path

/**
 * Document conversions via the LibreOffice CLI:
 *   soffice --headless --convert-to <target> --outdir <dir> <input>
 */
@Component
class LibreOfficeConverter(
    private val props: AppProperties,
    private val runner: ProcessRunner,
) : Converter {

    /** LibreOffice needs an export filter for some targets to avoid ambiguity. */
    private fun convertToArg(format: String): String = when (format.uppercase()) {
        "DOCX" -> "docx:MS Word 2007 XML"
        "DOC" -> "doc:MS Word 97"
        "TXT" -> "txt:Text"
        "ODT" -> "odt"
        "PDF" -> "pdf"
        else -> format.lowercase()
    }

    override fun convert(input: Path, outputFormat: String, settings: ConversionSettings, workDir: Path): Path {
        val targetExt = outputFormat.lowercase()
        val inputExt = input.fileName.toString().substringAfterLast('.', "").lowercase()

        val command = buildList {
            add(props.tools.sofficePath)
            // Isolated per-job profile so concurrent headless runs don't clash on the lock.
            add("-env:UserInstallation=${workDir.resolve("lo-profile").toUri()}")
            add("--headless"); add("--norestore"); add("--nolockcheck")
            // A PDF opens in Draw by default, which has no Writer export filter (the export
            // fails). Forcing the Writer PDF import loads it as a Writer doc so it can be
            // exported to docx/doc/txt/odt/rtf.
            if (inputExt == "pdf" && outputFormat.uppercase() in WRITER_TARGETS) {
                add("--infilter=writer_pdf_import")
            }
            add("--convert-to"); add(convertToArg(outputFormat))
            add("--outdir"); add(workDir.toString())
            add(input.toString())
        }
        runner.run(command, props.tools.timeoutSeconds)

        val baseName = input.fileName.toString().substringBeforeLast('.')
        val produced = workDir.resolve("$baseName.$targetExt")
        if (!Files.exists(produced)) {
            throw ConversionFailedException("LibreOffice did not produce $baseName.$targetExt")
        }
        return produced
    }

    private companion object {
        /** Word-processing targets that require the Writer (not Draw) PDF import. */
        val WRITER_TARGETS = setOf("DOCX", "DOC", "TXT", "ODT", "RTF")
    }
}

package uz.murodjon.filemaster.conversion.engine

import org.springframework.stereotype.Component
import uz.murodjon.filemaster.exception.ConversionFailedException
import java.nio.file.Files
import java.nio.file.Path
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/** Packs several files into one .zip via java.util.zip (no external tool needed). */
@Component
class ZipArchiver {

    /**
     * Writes a zip at [output] containing [inputs], each entry named by the matching [names] (the
     * user's original filename). Duplicate names are de-duplicated with a numeric suffix.
     */
    fun zip(inputs: List<Path>, names: List<String>, output: Path) {
        if (inputs.isEmpty()) throw ConversionFailedException("No files to zip.")
        val used = mutableSetOf<String>()
        ZipOutputStream(Files.newOutputStream(output)).use { zos ->
            inputs.forEachIndexed { i, path ->
                val entryName = uniqueName(names.getOrElse(i) { path.fileName.toString() }, used)
                zos.putNextEntry(ZipEntry(entryName))
                Files.newInputStream(path).use { it.copyTo(zos) }
                zos.closeEntry()
            }
        }
    }

    private fun uniqueName(name: String, used: MutableSet<String>): String {
        if (used.add(name)) return name
        val dot = name.lastIndexOf('.')
        val stem = if (dot > 0) name.substring(0, dot) else name
        val ext = if (dot > 0) name.substring(dot) else ""
        var n = 1
        var candidate: String
        do {
            candidate = "$stem-$n$ext"
            n++
        } while (!used.add(candidate))
        return candidate
    }
}

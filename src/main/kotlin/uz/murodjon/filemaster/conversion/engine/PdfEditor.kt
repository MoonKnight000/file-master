package uz.murodjon.filemaster.conversion.engine

import org.apache.pdfbox.Loader
import org.apache.pdfbox.pdmodel.PDDocument
import org.springframework.stereotype.Component
import uz.murodjon.filemaster.exception.ConversionFailedException
import java.nio.file.Path

/** Rotates, splits, deletes, extracts or reorders PDF pages via Apache PDFBox (no external tool needed). */
@Component
class PdfEditor {

    /** Deletes the pages selected by [ranges] ("2,5-7", 1-based), writing the rest to [output]. */
    fun deletePages(input: Path, ranges: String, output: Path) {
        Loader.loadPDF(input.toFile()).use { document ->
            val doomed = selectPages(ranges, document.numberOfPages)
            if (doomed.size >= document.numberOfPages) {
                throw ConversionFailedException("Cannot delete every page of the PDF.")
            }
            doomed.sortedDescending().forEach { p -> document.removePage(p - 1) }
            document.save(output.toFile())
        }
    }

    /** Copies only the pages selected by [ranges] ("2,5-7", 1-based, in document order) into ONE new PDF. */
    fun extractPages(input: Path, ranges: String, output: Path) {
        Loader.loadPDF(input.toFile()).use { document ->
            val pages = selectPages(ranges, document.numberOfPages)
            PDDocument().use { part ->
                pages.forEach { p -> part.importPage(document.getPage(p - 1)) }
                part.save(output.toFile())
            }
        }
    }

    /** Rewrites the PDF with pages in [order] ("3,1,2") — must be a full permutation of 1..pageCount. */
    fun reorderPages(input: Path, order: String, output: Path) {
        Loader.loadPDF(input.toFile()).use { document ->
            val pageCount = document.numberOfPages
            val pages = order.split(',').map { token ->
                token.trim().toIntOrNull() ?: throw ConversionFailedException("Invalid page number: ${token.trim()}")
            }
            if (pages.sorted() != (1..pageCount).toList()) {
                throw ConversionFailedException("Page order must list every page 1..$pageCount exactly once.")
            }
            PDDocument().use { reordered ->
                pages.forEach { p -> reordered.importPage(document.getPage(p - 1)) }
                reordered.save(output.toFile())
            }
        }
    }

    /** Rotates every page of [input] by [degrees] (added to any existing rotation), writing [output]. */
    fun rotate(input: Path, degrees: Int, output: Path) {
        Loader.loadPDF(input.toFile()).use { document ->
            document.pages.forEach { page ->
                page.rotation = (page.rotation + degrees) % 360
            }
            document.save(output.toFile())
        }
    }

    /**
     * Splits [input] into one PDF per range. [ranges] is `"1-3,5,7-9"` (1-based, inclusive); blank
     * or null splits into one PDF per page. Produced files land in [workDir] as `<base>-<label>.pdf`
     * and are returned in order.
     */
    fun split(input: Path, ranges: String?, workDir: Path): List<Path> {
        val base = input.fileName.toString().substringBeforeLast('.')
        Loader.loadPDF(input.toFile()).use { document ->
            val pageCount = document.numberOfPages
            val groups = parseRanges(ranges, pageCount)
            if (groups.isEmpty()) throw ConversionFailedException("No pages selected to split.")

            val outputs = mutableListOf<Path>()
            groups.forEach { (label, pages) ->
                PDDocument().use { part ->
                    // importPage deep-copies the page (and its resources) into the new document.
                    pages.forEach { p -> part.importPage(document.getPage(p - 1)) }
                    val out = workDir.resolve("$base-$label.pdf")
                    part.save(out.toFile())
                    outputs.add(out)
                }
            }
            return outputs
        }
    }

    /**
     * Parses a flat `"2,5-7"` selection into a sorted, deduped 1-based page list (delete/extract).
     * Unlike [parseRanges], a blank spec is an error — these edits require an explicit selection.
     */
    private fun selectPages(spec: String, pageCount: Int): List<Int> {
        if (spec.isBlank()) throw ConversionFailedException("No pages selected.")
        val pages = parseRanges(spec, pageCount).flatMap { it.second }.distinct().sorted()
        if (pages.isEmpty()) throw ConversionFailedException("No pages selected.")
        return pages
    }

    /** Parses `"1-3,5"` into (label, 1-based page list) groups, clamped to [pageCount]. */
    private fun parseRanges(ranges: String?, pageCount: Int): List<Pair<String, List<Int>>> {
        val spec = ranges?.trim().orEmpty()
        if (spec.isBlank()) {
            return (1..pageCount).map { "p$it" to listOf(it) }
        }
        return spec.split(',').mapNotNull { token ->
            val t = token.trim()
            if (t.isEmpty()) return@mapNotNull null
            if ('-' in t) {
                val (a, b) = t.split('-', limit = 2)
                val start = a.trim().toIntOrNull() ?: throw ConversionFailedException("Invalid range: $t")
                val end = b.trim().toIntOrNull() ?: throw ConversionFailedException("Invalid range: $t")
                val lo = start.coerceIn(1, pageCount)
                val hi = end.coerceIn(1, pageCount)
                if (lo > hi) throw ConversionFailedException("Invalid range: $t")
                "p$lo-$hi" to (lo..hi).toList()
            } else {
                val n = t.toIntOrNull() ?: throw ConversionFailedException("Invalid page: $t")
                if (n !in 1..pageCount) throw ConversionFailedException("Page $n out of range (1..$pageCount).")
                "p$n" to listOf(n)
            }
        }
    }
}

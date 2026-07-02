package uz.murodjon.filemaster.conversion.engine

import org.apache.pdfbox.io.IOUtils
import org.apache.pdfbox.multipdf.PDFMergerUtility
import org.springframework.stereotype.Component
import java.nio.file.Path

/** Combines several PDFs into one, via Apache PDFBox (no external tool needed). */
@Component
class PdfMerger {

    fun merge(inputs: List<Path>, output: Path) {
        val merger = PDFMergerUtility()
        merger.destinationFileName = output.toString()
        inputs.forEach { merger.addSource(it.toFile()) }
        merger.mergeDocuments(IOUtils.createMemoryOnlyStreamCache())
    }
}

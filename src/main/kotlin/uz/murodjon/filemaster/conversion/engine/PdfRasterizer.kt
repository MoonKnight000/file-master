package uz.murodjon.filemaster.conversion.engine

import org.apache.pdfbox.Loader
import org.apache.pdfbox.rendering.ImageType
import org.apache.pdfbox.rendering.PDFRenderer
import org.springframework.stereotype.Component
import uz.murodjon.filemaster.common.Quality
import uz.murodjon.filemaster.exception.ConversionFailedException
import java.nio.file.Path
import javax.imageio.ImageIO

/** Renders each page of a PDF to a JPG/PNG image via Apache PDFBox (no external tool needed). */
@Component
class PdfRasterizer {

    /** Higher quality preset -> higher render DPI. */
    private fun dpiFor(quality: Quality): Float = when (quality) {
        Quality.HIGH -> 200f
        Quality.BALANCED -> 150f
        Quality.SMALL -> 96f
    }

    /**
     * Renders [input] page-by-page, writing `<base>-p<N>.<ext>` files into [workDir]; returns the
     * produced image paths in page order (JPG pages are flattened onto white — JPEG has no alpha).
     */
    fun rasterize(input: Path, outputFormat: String, quality: Quality, workDir: Path): List<Path> {
        val ext = outputFormat.lowercase().let { if (it == "jpeg") "jpg" else it }
        val base = input.fileName.toString().substringBeforeLast('.')
        val dpi = dpiFor(quality)
        val imageType = if (ext == "jpg") ImageType.RGB else ImageType.ARGB

        val outputs = mutableListOf<Path>()
        Loader.loadPDF(input.toFile()).use { document ->
            val renderer = PDFRenderer(document)
            for (page in 0 until document.numberOfPages) {
                val image = renderer.renderImageWithDPI(page, dpi, imageType)
                val out = workDir.resolve("$base-p${page + 1}.$ext")
                if (!ImageIO.write(image, ext, out.toFile())) {
                    throw ConversionFailedException("Unsupported image output format: $outputFormat")
                }
                outputs.add(out)
            }
        }
        if (outputs.isEmpty()) throw ConversionFailedException("PDF has no pages to render.")
        return outputs
    }
}

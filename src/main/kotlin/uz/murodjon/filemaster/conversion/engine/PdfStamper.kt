package uz.murodjon.filemaster.conversion.engine

import org.apache.pdfbox.Loader
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.font.PDType1Font
import org.apache.pdfbox.pdmodel.font.Standard14Fonts
import org.apache.pdfbox.pdmodel.graphics.state.PDExtendedGraphicsState
import org.apache.pdfbox.util.Matrix
import org.springframework.stereotype.Component
import java.nio.file.Path

/** Stamps text overlays — watermarks and page numbers — onto PDF pages via Apache PDFBox. */
@Component
class PdfStamper {

    private val font = PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD)

    /**
     * Stamps [text] on every page at [position] (catalog vocabulary; `"diagonal"` = rotated 45°
     * across the page center) with the given [opacity] (0..1) and [fontSize], writing [output].
     */
    fun watermark(input: Path, text: String, position: String, opacity: Double, fontSize: Int, output: Path) {
        Loader.loadPDF(input.toFile()).use { document ->
            val alpha = PDExtendedGraphicsState().apply {
                nonStrokingAlphaConstant = opacity.toFloat()
            }
            document.pages.forEach { page ->
                PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND, true, true).use { cs ->
                    cs.setGraphicsStateParameters(alpha)
                    cs.beginText()
                    cs.setFont(font, fontSize.toFloat())
                    val box = page.mediaBox
                    val textWidth = font.getStringWidth(text) / 1000 * fontSize
                    if (position == "diagonal") {
                        val matrix = Matrix()
                        matrix.translate(box.width / 2, box.height / 2)
                        matrix.rotate(Math.toRadians(45.0))
                        matrix.translate(-textWidth / 2, -fontSize / 2f)
                        cs.setTextMatrix(matrix)
                    } else {
                        val (x, y) = positionOf(position, box.width, box.height, textWidth, fontSize.toFloat())
                        cs.newLineAtOffset(x, y)
                    }
                    cs.showText(text)
                    cs.endText()
                }
            }
            document.save(output.toFile())
        }
    }

    /** Stamps "1", "2", … on each page at [position] (page-number vocabulary), writing [output]. */
    fun addPageNumbers(input: Path, position: String, output: Path) {
        Loader.loadPDF(input.toFile()).use { document ->
            document.pages.forEachIndexed { index, page ->
                PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND, true, true).use { cs ->
                    val text = (index + 1).toString()
                    val box = page.mediaBox
                    val textWidth = font.getStringWidth(text) / 1000 * PAGE_NUMBER_FONT_SIZE
                    val (x, y) = positionOf(position, box.width, box.height, textWidth, PAGE_NUMBER_FONT_SIZE)
                    cs.beginText()
                    cs.setFont(font, PAGE_NUMBER_FONT_SIZE)
                    cs.newLineAtOffset(x, y)
                    cs.showText(text)
                    cs.endText()
                }
            }
            document.save(output.toFile())
        }
    }

    /** Maps a position token to the text origin, keeping a [MARGIN] from the page edges. */
    private fun positionOf(position: String, pageWidth: Float, pageHeight: Float, textWidth: Float, fontSize: Float): Pair<Float, Float> {
        val x = when {
            position.endsWith("left") -> MARGIN
            position.endsWith("right") -> pageWidth - textWidth - MARGIN
            else -> (pageWidth - textWidth) / 2               // *-center / center
        }
        val y = when {
            position.startsWith("top") -> pageHeight - fontSize - MARGIN
            position.startsWith("bottom") -> MARGIN
            else -> (pageHeight - fontSize) / 2               // center
        }
        return x to y
    }

    companion object {
        private const val MARGIN = 24f
        private const val PAGE_NUMBER_FONT_SIZE = 12f
    }
}

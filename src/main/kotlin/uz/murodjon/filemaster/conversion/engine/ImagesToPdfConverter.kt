package uz.murodjon.filemaster.conversion.engine

import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject
import org.springframework.stereotype.Component
import uz.murodjon.filemaster.exception.ConversionFailedException
import java.nio.file.Path
import kotlin.math.min

/** Combines several images into one PDF (one image per page, scaled to fit), via Apache PDFBox. */
@Component
class ImagesToPdfConverter {

    /** Writes a PDF to [output] with one page per input image, each centered and scaled to fit A4. */
    fun combine(images: List<Path>, output: Path) {
        if (images.isEmpty()) throw ConversionFailedException("No images to combine.")
        PDDocument().use { doc ->
            images.forEach { imagePath ->
                val pdImage = PDImageXObject.createFromFile(imagePath.toString(), doc)
                val page = PDPage(PDRectangle.A4)
                doc.addPage(page)

                val box = page.mediaBox
                val scale = min(box.width / pdImage.width, box.height / pdImage.height)
                val w = pdImage.width * scale
                val h = pdImage.height * scale
                val x = (box.width - w) / 2
                val y = (box.height - h) / 2

                PDPageContentStream(doc, page).use { stream ->
                    stream.drawImage(pdImage, x, y, w, h)
                }
            }
            doc.save(output.toFile())
        }
    }
}

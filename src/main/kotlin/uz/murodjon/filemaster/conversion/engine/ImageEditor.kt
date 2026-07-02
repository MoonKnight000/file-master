package uz.murodjon.filemaster.conversion.engine

import org.springframework.stereotype.Component
import uz.murodjon.filemaster.exception.ConversionFailedException
import java.awt.AlphaComposite
import java.awt.Color
import java.awt.Font
import java.awt.RenderingHints
import java.awt.color.ColorSpace
import java.awt.image.BufferedImage
import java.awt.image.ColorConvertOp
import java.awt.image.ConvolveOp
import java.awt.image.Kernel
import kotlin.math.roundToInt

/**
 * Pure-Java BufferedImage edit toolbox (crop, flip, filters, adjustments, text watermark).
 * Each method returns a new image; [uz.murodjon.filemaster.conversion.engine.ImageConverter]
 * chains them from [ConversionSettings].
 */
@Component
class ImageEditor {

    /** Crops the [x],[y],[w]×[h] rectangle (original-image pixels), copying out of the shared raster. */
    fun crop(img: BufferedImage, x: Int, y: Int, w: Int, h: Int): BufferedImage {
        if (x < 0 || y < 0 || w < 1 || h < 1 || x + w > img.width || y + h > img.height) {
            throw ConversionFailedException(
                "Crop rectangle ${w}x$h at ($x,$y) does not fit the ${img.width}x${img.height} image.")
        }
        // getSubimage shares the raster — copy so later edit steps cannot corrupt it.
        val sub = img.getSubimage(x, y, w, h)
        val copy = BufferedImage(w, h, typeOf(img))
        val g = copy.createGraphics()
        g.drawImage(sub, 0, 0, null)
        g.dispose()
        return copy
    }

    /** Mirrors the image; [direction] is "horizontal" (default) or "vertical". */
    fun flip(img: BufferedImage, direction: String): BufferedImage {
        val out = BufferedImage(img.width, img.height, typeOf(img))
        val g = out.createGraphics()
        // Draw with swapped destination corners — works for every source image type,
        // unlike AffineTransformOp which rejects some JPEG color models.
        if (direction == "vertical") {
            g.drawImage(img, 0, img.height, img.width, 0, 0, 0, img.width, img.height, null)
        } else {
            g.drawImage(img, img.width, 0, 0, img.height, 0, 0, img.width, img.height, null)
        }
        g.dispose()
        return out
    }

    /** Applies a named filter: grayscale, sepia, invert, blur or sharpen. */
    fun applyFilter(img: BufferedImage, filter: String): BufferedImage = when (filter) {
        "grayscale" -> ColorConvertOp(ColorSpace.getInstance(ColorSpace.CS_GRAY), null)
            .filter(img, BufferedImage(img.width, img.height, typeOf(img)))
        "invert" -> mapPixels(img) { r, g, b -> Triple(255 - r, 255 - g, 255 - b) }
        "sepia" -> mapPixels(img) { r, g, b ->
            Triple(
                (0.393 * r + 0.769 * g + 0.189 * b).roundToInt().coerceAtMost(255),
                (0.349 * r + 0.686 * g + 0.168 * b).roundToInt().coerceAtMost(255),
                (0.272 * r + 0.534 * g + 0.131 * b).roundToInt().coerceAtMost(255),
            )
        }
        "blur" -> convolve(img, FloatArray(9) { 1f / 9f })
        "sharpen" -> convolve(img, floatArrayOf(0f, -1f, 0f, -1f, 5f, -1f, 0f, -1f, 0f))
        else -> throw ConversionFailedException("Unknown image filter: $filter")
    }

    /** Adjusts [brightness]/[contrast]/[saturation], each -100..100 (null = unchanged). */
    fun adjust(img: BufferedImage, brightness: Int?, contrast: Int?, saturation: Int?): BufferedImage {
        val b = (brightness ?: 0) / 100.0    // -1..1 → shift of up to ±255
        val c = (contrast ?: 0) / 100.0      // -1..1 → slope 0..2 around the midpoint
        val s = (saturation ?: 0) / 100.0    // -1..1 → HSB saturation scale 0..2
        if (b == 0.0 && c == 0.0 && s == 0.0) return img
        val slope = 1.0 + c
        return mapPixels(img) { r0, g0, b0 ->
            var (r, g, bl) = Triple(r0, g0, b0)
            if (s != 0.0) {
                val hsb = Color.RGBtoHSB(r, g, bl, null)
                val rgb = Color.HSBtoRGB(hsb[0], (hsb[1] * (1.0 + s)).toFloat().coerceIn(0f, 1f), hsb[2])
                r = (rgb shr 16) and 0xFF; g = (rgb shr 8) and 0xFF; bl = rgb and 0xFF
            }
            Triple(level(r, slope, b), level(g, slope, b), level(bl, slope, b))
        }
    }

    /** Draws [text] over the image at [position] ("diagonal" = 45° across the center). */
    fun watermark(img: BufferedImage, text: String, position: String, opacity: Double, fontSize: Int): BufferedImage {
        val out = BufferedImage(img.width, img.height, typeOf(img))
        val g = out.createGraphics()
        g.drawImage(img, 0, 0, null)
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)
        g.composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity.toFloat().coerceIn(0f, 1f))
        g.font = Font(Font.SANS_SERIF, Font.BOLD, fontSize)
        g.color = Color.WHITE
        val bounds = g.fontMetrics.getStringBounds(text, g)
        val textWidth = bounds.width.toFloat()
        val ascent = g.fontMetrics.ascent.toFloat()
        if (position == "diagonal") {
            g.rotate(Math.toRadians(-45.0), img.width / 2.0, img.height / 2.0)
            g.drawString(text, img.width / 2f - textWidth / 2, img.height / 2f + ascent / 2)
        } else {
            val x = when {
                position.endsWith("left") -> MARGIN
                position.endsWith("right") -> img.width - textWidth - MARGIN
                else -> (img.width - textWidth) / 2
            }
            val y = when {
                position.startsWith("top") -> ascent + MARGIN
                position.startsWith("bottom") -> img.height - MARGIN
                else -> (img.height + ascent) / 2
            }
            g.drawString(text, x, y)
        }
        g.dispose()
        return out
    }

    /** Brightness/contrast levelling: contrast slope around mid-gray, then brightness shift. */
    private fun level(channel: Int, slope: Double, brightness: Double): Int =
        ((channel - 128) * slope + 128 + brightness * 255).roundToInt().coerceIn(0, 255)

    private fun mapPixels(img: BufferedImage, mapper: (r: Int, g: Int, b: Int) -> Triple<Int, Int, Int>): BufferedImage {
        val out = BufferedImage(img.width, img.height, typeOf(img))
        for (y in 0 until img.height) {
            for (x in 0 until img.width) {
                val argb = img.getRGB(x, y)
                val (r, g, b) = mapper((argb shr 16) and 0xFF, (argb shr 8) and 0xFF, argb and 0xFF)
                out.setRGB(x, y, (argb and 0xFF000000.toInt()) or (r shl 16) or (g shl 8) or b)
            }
        }
        return out
    }

    private fun convolve(img: BufferedImage, kernel: FloatArray): BufferedImage =
        ConvolveOp(Kernel(3, 3, kernel), ConvolveOp.EDGE_NO_OP, null)
            .filter(img, BufferedImage(img.width, img.height, typeOf(img)))

    private fun typeOf(img: BufferedImage): Int =
        if (img.transparency == BufferedImage.OPAQUE) BufferedImage.TYPE_INT_RGB else BufferedImage.TYPE_INT_ARGB

    companion object {
        private const val MARGIN = 24f
    }
}

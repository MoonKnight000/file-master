package uz.murodjon.filemaster.conversion.engine

import org.springframework.stereotype.Component
import uz.murodjon.filemaster.common.Quality
import uz.murodjon.filemaster.exception.ConversionFailedException
import java.awt.Color
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import javax.imageio.IIOImage
import javax.imageio.ImageIO
import javax.imageio.ImageWriteParam
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/** Image compression / conversion / editing (jpg, png) via pure-Java ImageIO — no external tool needed. */
@Component
class ImageConverter(private val editor: ImageEditor) : Converter {

    override fun convert(input: Path, outputFormat: String, settings: ConversionSettings, workDir: Path): Path {
        val targetExt = outputFormat.lowercase().let { if (it == "jpeg") "jpg" else it }
        val baseName = input.fileName.toString().substringBeforeLast('.')
        val output = workDir.resolve("$baseName.$targetExt")

        val source = ImageIO.read(input.toFile())
            ?: throw ConversionFailedException("Could not read image: ${input.fileName}")
        // Edit chain: crop first (coordinates refer to the original), watermark last (undistorted).
        var image = source
        if (settings.cropWidth != null && settings.cropHeight != null) {
            image = editor.crop(image, settings.cropX ?: 0, settings.cropY ?: 0, settings.cropWidth, settings.cropHeight)
        }
        image = resize(image, settings.imageWidth, settings.imageHeight)
        image = rotate(image, settings.rotateDegrees)
        settings.flipDirection?.let { image = editor.flip(image, it) }
        settings.imageFilter?.let { image = editor.applyFilter(image, it) }
        if (settings.brightness != null || settings.contrast != null || settings.saturation != null) {
            image = editor.adjust(image, settings.brightness, settings.contrast, settings.saturation)
        }
        settings.watermarkText?.takeIf { it.isNotBlank() }?.let { text ->
            image = editor.watermark(image, text, settings.watermarkPosition ?: "diagonal",
                settings.watermarkOpacity ?: 0.3, settings.watermarkFontSize ?: 48)
        }

        if (targetExt == "jpg") {
            val flat = flatten(image)
            val target = settings.targetBytes
            if (target != null && target > 0) {
                compressJpegToTarget(flat, output.toFile(), target)
            } else {
                writeJpeg(flat, output.toFile(), qualityFactor(settings))
            }
        } else {
            val ok = ImageIO.write(image, targetExt, output.toFile())
            if (!ok) throw ConversionFailedException("Unsupported image output format: $outputFormat")
        }

        if (!Files.exists(output)) throw ConversionFailedException("Image conversion produced no file.")
        return output
    }

    /** Explicit `imageQuality` (1..100) wins; otherwise derive from the quality preset. */
    private fun qualityFactor(settings: ConversionSettings): Float =
        settings.imageQuality?.let { (it / 100f).coerceIn(0.01f, 1f) } ?: when (settings.quality) {
            Quality.HIGH -> 0.92f
            Quality.BALANCED -> 0.75f
            Quality.SMALL -> 0.5f
        }

    /** Scales to fit within the given bound(s), keeping aspect ratio. No bound = unchanged. */
    private fun resize(img: BufferedImage, width: Int?, height: Int?): BufferedImage {
        if (width == null && height == null) return img
        val tw = width?.toDouble()
        val th = height?.toDouble()
        val scale = when {
            tw != null && th != null -> min(tw / img.width, th / img.height)
            tw != null -> tw / img.width
            else -> th!! / img.height
        }
        val newW = max(1, (img.width * scale).roundToInt())
        val newH = max(1, (img.height * scale).roundToInt())
        if (newW == img.width && newH == img.height) return img

        val type = if (img.transparency == BufferedImage.OPAQUE) BufferedImage.TYPE_INT_RGB else BufferedImage.TYPE_INT_ARGB
        val scaled = BufferedImage(newW, newH, type)
        val g = scaled.createGraphics()
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR)
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)
        g.drawImage(img, 0, 0, newW, newH, null)
        g.dispose()
        return scaled
    }

    /** Rotates by a quarter-turn multiple (90/180/270). null/0 = unchanged. */
    private fun rotate(img: BufferedImage, degrees: Int?): BufferedImage {
        val normalized = ((degrees ?: 0) % 360 + 360) % 360
        if (normalized == 0) return img
        val swap = normalized == 90 || normalized == 270
        val newW = if (swap) img.height else img.width
        val newH = if (swap) img.width else img.height

        val type = if (img.transparency == BufferedImage.OPAQUE) BufferedImage.TYPE_INT_RGB else BufferedImage.TYPE_INT_ARGB
        val rotated = BufferedImage(newW, newH, type)
        val g = rotated.createGraphics()
        g.translate((newW - img.width) / 2, (newH - img.height) / 2)
        g.rotate(Math.toRadians(normalized.toDouble()), (img.width / 2).toDouble(), (img.height / 2).toDouble())
        g.drawImage(img, 0, 0, null)
        g.dispose()
        return rotated
    }

    private fun flatten(img: BufferedImage): BufferedImage {
        if (img.transparency == BufferedImage.OPAQUE) return img
        val rgb = BufferedImage(img.width, img.height, BufferedImage.TYPE_INT_RGB)
        val g = rgb.createGraphics()
        g.color = Color.WHITE
        g.fillRect(0, 0, img.width, img.height)
        g.drawImage(img, 0, 0, null)
        g.dispose()
        return rgb
    }

    /**
     * Binary-search on JPEG quality to find the highest quality that fits within [targetBytes].
     * 12 iterations → precision < 1 quality point. Falls back to quality=1 if nothing fits.
     */
    private fun compressJpegToTarget(img: BufferedImage, dest: File, targetBytes: Long) {
        var lo = 1; var hi = 100; var bestQuality = 1
        repeat(12) {
            val mid = (lo + hi) / 2
            val buf = java.io.ByteArrayOutputStream()
            writeJpegToStream(img, buf, mid / 100f)
            if (buf.size() <= targetBytes) { bestQuality = mid; lo = mid + 1 } else { hi = mid - 1 }
        }
        writeJpeg(img, dest, bestQuality / 100f)
    }

    private fun writeJpegToStream(img: BufferedImage, out: java.io.OutputStream, quality: Float) {
        val writer = ImageIO.getImageWritersByFormatName("jpg").next()
        ImageIO.createImageOutputStream(out).use { ios ->
            writer.output = ios
            val param = writer.defaultWriteParam.apply {
                if (canWriteCompressed()) { compressionMode = ImageWriteParam.MODE_EXPLICIT; compressionQuality = quality }
            }
            writer.write(null, IIOImage(img, null, null), param)
        }
        writer.dispose()
    }

    private fun writeJpeg(img: BufferedImage, dest: File, quality: Float) {
        val writer = ImageIO.getImageWritersByFormatName("jpg").next()
        ImageIO.createImageOutputStream(dest).use { ios ->
            writer.output = ios
            val param = writer.defaultWriteParam.apply {
                if (canWriteCompressed()) {
                    compressionMode = ImageWriteParam.MODE_EXPLICIT
                    compressionQuality = quality
                }
            }
            writer.write(null, IIOImage(img, null, null), param)
        }
        writer.dispose()
    }
}

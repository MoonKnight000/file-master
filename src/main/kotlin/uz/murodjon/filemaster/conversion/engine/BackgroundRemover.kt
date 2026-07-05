package uz.murodjon.filemaster.conversion.engine

import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import uz.murodjon.filemaster.config.AppProperties
import uz.murodjon.filemaster.exception.ConversionFailedException
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.nio.FloatBuffer
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import javax.imageio.ImageIO

/**
 * AI background removal with the u2net salient-object model, run in-JVM through ONNX
 * Runtime (CPU) — no external process. The image is letterbox-free resized to the
 * model's 320x320 input, the predicted saliency mask is min-max normalized, scaled back
 * to the original size and applied as the alpha channel; output is always PNG.
 * Model file: `app.tools.u2net-model-path` (download: rembg's u2net.onnx release asset).
 */
@Component
class BackgroundRemover(private val props: AppProperties) : Converter {

    private val log = LoggerFactory.getLogger(javaClass)

    private companion object {
        const val SIDE = 320
        // ImageNet normalization u2net was trained with.
        val MEAN = floatArrayOf(0.485f, 0.456f, 0.406f)
        val STD = floatArrayOf(0.229f, 0.224f, 0.225f)
    }

    // The session is expensive (~170MB model); load once, lazily, and share — OrtSession.run is thread-safe.
    @Volatile
    private var session: OrtSession? = null

    private fun session(): OrtSession {
        session?.let { return it }
        synchronized(this) {
            session?.let { return it }
            val modelPath = Paths.get(props.tools.u2netModelPath)
            if (!Files.exists(modelPath)) {
                throw ConversionFailedException("Background-removal model is not installed on the server.")
            }
            log.info("Loading u2net model from {}", modelPath)
            val env = OrtEnvironment.getEnvironment()
            return env.createSession(modelPath.toString(), OrtSession.SessionOptions()).also { session = it }
        }
    }

    override fun convert(input: Path, outputFormat: String, settings: ConversionSettings, workDir: Path): Path {
        val original = ImageIO.read(input.toFile())
            ?: throw ConversionFailedException("Could not read the image (unsupported or corrupt file).")

        val mask = predictMask(original)
        val cutout = applyAlpha(original, mask)

        val output = workDir.resolve(input.fileName.toString().substringBeforeLast('.') + "-no-bg.png")
        ImageIO.write(cutout, "png", output.toFile())
        return output
    }

    /** Runs u2net and returns the saliency mask scaled back to the original image size. */
    private fun predictMask(original: BufferedImage): BufferedImage {
        val resized = scale(original, SIDE, SIDE, BufferedImage.TYPE_INT_RGB)

        val chw = FloatArray(3 * SIDE * SIDE)
        for (y in 0 until SIDE) {
            for (x in 0 until SIDE) {
                val rgb = resized.getRGB(x, y)
                val i = y * SIDE + x
                chw[i] = (((rgb shr 16 and 0xFF) / 255f) - MEAN[0]) / STD[0]
                chw[SIDE * SIDE + i] = (((rgb shr 8 and 0xFF) / 255f) - MEAN[1]) / STD[1]
                chw[2 * SIDE * SIDE + i] = (((rgb and 0xFF) / 255f) - MEAN[2]) / STD[2]
            }
        }

        val env = OrtEnvironment.getEnvironment()
        val scores = OnnxTensor.createTensor(env, FloatBuffer.wrap(chw), longArrayOf(1, 3, SIDE.toLong(), SIDE.toLong())).use { tensor ->
            session().run(mapOf(session().inputNames.first() to tensor)).use { results ->
                @Suppress("UNCHECKED_CAST")
                val out = results[0].value as Array<Array<Array<FloatArray>>> // [1][1][320][320]
                out[0][0]
            }
        }

        var min = Float.MAX_VALUE
        var max = -Float.MAX_VALUE
        scores.forEach { row -> row.forEach { v -> if (v < min) min = v; if (v > max) max = v } }
        val range = (max - min).takeIf { it > 1e-6f } ?: 1f

        val mask = BufferedImage(SIDE, SIDE, BufferedImage.TYPE_BYTE_GRAY)
        for (y in 0 until SIDE) {
            for (x in 0 until SIDE) {
                val a = (((scores[y][x] - min) / range) * 255f).toInt().coerceIn(0, 255)
                mask.raster.setSample(x, y, 0, a)
            }
        }
        return scale(mask, original.width, original.height, BufferedImage.TYPE_BYTE_GRAY)
    }

    private fun applyAlpha(original: BufferedImage, mask: BufferedImage): BufferedImage {
        val out = BufferedImage(original.width, original.height, BufferedImage.TYPE_INT_ARGB)
        for (y in 0 until original.height) {
            for (x in 0 until original.width) {
                val alpha = mask.raster.getSample(x, y, 0)
                out.setRGB(x, y, (alpha shl 24) or (original.getRGB(x, y) and 0xFFFFFF))
            }
        }
        return out
    }

    private fun scale(src: BufferedImage, w: Int, h: Int, type: Int): BufferedImage {
        val dst = BufferedImage(w, h, type)
        val g = dst.createGraphics()
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR)
        g.drawImage(src, 0, 0, w, h, null)
        g.dispose()
        return dst
    }
}

package uz.murodjon.filemaster.image.model

import jakarta.persistence.Entity
import jakarta.persistence.Table
import uz.murodjon.filemaster.auth.model.User
import uz.murodjon.filemaster.common.Quality
import uz.murodjon.filemaster.conversion.engine.ConversionSettings
import uz.murodjon.filemaster.conversion.model.ConversionJob
import uz.murodjon.filemaster.tools.enums.ToolSlug

/**
 * Image conversion job: carries the image-only knobs (joined to [ConversionJob] on its id).
 * These columns live here and nowhere else — no other category reads them.
 */
@Entity
@Table(name = "image_conversion_jobs")
class ImageConversionJob(
    user: User,
    tool: ToolSlug,
    outputFormat: String,
    quality: Quality = Quality.BALANCED,
    keepOriginal: Boolean = true,
    mergeIntoOne: Boolean = false,
    var imageWidth: Int? = null,
    var imageHeight: Int? = null,
    var imageQuality: Int? = null,
    var targetBytes: Long? = null,
    var rotateDegrees: Int? = null,
) : ConversionJob(user, tool, outputFormat, quality, keepOriginal, mergeIntoOne) {

    override fun toSettings(): ConversionSettings = super.toSettings().copy(
        imageWidth = imageWidth,
        imageHeight = imageHeight,
        imageQuality = imageQuality,
        targetBytes = targetBytes,
        rotateDegrees = rotateDegrees,
    )
}

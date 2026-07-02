package uz.murodjon.filemaster.pdf.model

import jakarta.persistence.Entity
import jakarta.persistence.Table
import uz.murodjon.filemaster.auth.model.User
import uz.murodjon.filemaster.common.Quality
import uz.murodjon.filemaster.conversion.engine.ConversionSettings
import uz.murodjon.filemaster.conversion.model.ConversionJob
import uz.murodjon.filemaster.tools.enums.ToolSlug

/**
 * Document conversion job: carries the PDF edit knobs (rotate angle, split ranges) joined to
 * [uz.murodjon.filemaster.conversion.model.ConversionJob] on its id. Plain document conversions (convert/merge/ocr) leave these null.
 */
@Entity
@Table(name = "document_conversion_jobs")
class DocumentConversionJob(
    user: User,
    tool: ToolSlug,
    outputFormat: String,
    quality: Quality = Quality.BALANCED,
    keepOriginal: Boolean = true,
    mergeIntoOne: Boolean = false,
    var rotateDegrees: Int? = null,
    var splitRanges: String? = null,
    var compress: Boolean = false,
    var ocrLanguage: String? = null,
) : ConversionJob(user, tool, outputFormat, quality, keepOriginal, mergeIntoOne) {

    override fun toSettings(): ConversionSettings = super.toSettings().copy(
        rotateDegrees = rotateDegrees,
        splitRanges = splitRanges,
        compress = compress,
        ocrLanguage = ocrLanguage,
    )
}
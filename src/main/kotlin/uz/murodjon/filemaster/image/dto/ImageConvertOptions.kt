package uz.murodjon.filemaster.image.dto

import uz.murodjon.filemaster.common.Quality

data class ImageConvertOptions(
    val outputFormat: String? = null,
    val quality: Quality? = null,
    val keepOriginal: Boolean? = null,
    val imageWidth: Int? = null,
    val imageHeight: Int? = null,
    val imageQuality: Int? = null,
    val targetBytes: Long? = null,   // compress-image: target output size in bytes (JPEG only)
    val rotateDegrees: Int? = null,  // rotate-image: 90 | 180 | 270
    val cropX: Int? = null,          // crop-image: rectangle in original-image pixels
    val cropY: Int? = null,
    val cropWidth: Int? = null,
    val cropHeight: Int? = null,
    val flipDirection: String? = null,  // flip-image: "horizontal" | "vertical"
    val imageFilter: String? = null,    // image-filter: grayscale | sepia | invert | blur | sharpen
    val brightness: Int? = null,        // adjust-image: -100..100
    val contrast: Int? = null,
    val saturation: Int? = null,
    val watermarkText: String? = null,  // watermark-image
    val watermarkPosition: String? = null,
    val watermarkOpacity: Double? = null,
    val watermarkFontSize: Int? = null,
)

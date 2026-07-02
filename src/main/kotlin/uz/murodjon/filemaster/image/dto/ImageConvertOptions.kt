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
)

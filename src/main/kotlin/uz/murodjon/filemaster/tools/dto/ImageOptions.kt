package uz.murodjon.filemaster.tools.dto

/** Allowed image knobs the front-end should render for image tools. */
data class ImageOptions(
    val maxDimension: Int,
    val minQuality: Int,
    val maxQuality: Int,
)

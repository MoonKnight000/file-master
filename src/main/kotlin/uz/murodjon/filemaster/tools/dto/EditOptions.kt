package uz.murodjon.filemaster.tools.dto

import uz.murodjon.filemaster.tools.enums.EditOperation

/**
 * The edit knobs the front-end should render for an `kind == EDIT` tool. [operation] selects the
 * form family; the other fields carry that family's vocabulary/ranges and are null for every other
 * operation (trim/split/resize/page-picker controls are derived from [operation] alone).
 */
data class EditOptions(
    val operation: EditOperation,
    val angles: List<Int>? = null,            // ROTATE
    val positions: List<String>? = null,      // WATERMARK / PAGE_NUMBERS
    val flipDirections: List<String>? = null, // FLIP
    val filters: List<String>? = null,        // FILTER
    val opacityMin: Double? = null,           // WATERMARK
    val opacityMax: Double? = null,
    val fontSizeMin: Int? = null,             // WATERMARK
    val fontSizeMax: Int? = null,
    val maxTextLength: Int? = null,           // WATERMARK
    val adjustMin: Int? = null,               // ADJUST
    val adjustMax: Int? = null,
    val passwordMinLength: Int? = null,       // PROTECT
    val passwordMaxLength: Int? = null,
    val speedMin: Double? = null,             // SPEED
    val speedMax: Double? = null,
    val volumeMin: Double? = null,            // VOLUME
    val volumeMax: Double? = null,
    val fadeMaxSeconds: Double? = null,       // FADE
)

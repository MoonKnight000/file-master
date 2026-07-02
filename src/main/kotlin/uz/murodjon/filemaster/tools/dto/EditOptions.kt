package uz.murodjon.filemaster.tools.dto

import uz.murodjon.filemaster.tools.enums.EditOperation

/**
 * The edit knobs the front-end should render for an `kind == EDIT` tool. [operation] selects the
 * form family; [angles] lists the allowed rotation steps (ROTATE only — null otherwise, the
 * front-end derives trim/split/resize controls from [operation]).
 */
data class EditOptions(
    val operation: EditOperation,
    val angles: List<Int>? = null,
)

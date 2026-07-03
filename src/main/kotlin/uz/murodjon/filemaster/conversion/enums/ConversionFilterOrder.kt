package uz.murodjon.filemaster.conversion.enums

import uz.murodjon.filemaster.pageable.TableField

/** Sortable columns of a conversion job (must be real [ConversionJob] properties). */
enum class ConversionFilterOrder(override val value: String) : TableField {
    CREATED_TIMESTAMP("createdTimestamp"),
    UPDATED_TIMESTAMP("updatedTimestamp"),
    STATUS("status"),
}

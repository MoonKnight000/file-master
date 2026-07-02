package uz.murodjon.filemaster.conversion.enums

import uz.murodjon.filemaster.pageable.TableField

enum class ConversionFilterOrder(override val value: String) : TableField {
    NAME("name"),
    SIZE("bytes"),
    CREATED_TIMESTAMP("createdTimestamp"),
    UPDATED_TIMESTAMP("updatedTimestamp")
}
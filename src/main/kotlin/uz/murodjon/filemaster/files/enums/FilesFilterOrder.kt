package uz.murodjon.filemaster.files.enums

import uz.murodjon.filemaster.pageable.TableField

enum class FilesFilterOrder(override val value: String) : TableField {
    NAME("name"),
    SIZE("bytes"),
    CREATED_TIMESTAMP("createdTimestamp"),
    UPDATED_TIMESTAMP("updatedTimestamp")
}
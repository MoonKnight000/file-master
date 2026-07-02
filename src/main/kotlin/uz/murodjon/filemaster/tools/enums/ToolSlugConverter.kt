package uz.murodjon.filemaster.tools.enums

import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

@Converter(autoApply = true)
class ToolSlugConverter : AttributeConverter<ToolSlug, String> {
    override fun convertToDatabaseColumn(attribute: ToolSlug?): String? = attribute?.slug
    override fun convertToEntityAttribute(dbData: String?): ToolSlug? =
        dbData?.let { ToolSlug.from(it) }
}

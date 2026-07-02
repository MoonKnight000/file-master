package uz.murodjon.filemaster.common

import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

@Converter(autoApply = true)
class QualityConverter : AttributeConverter<Quality, String> {
    override fun convertToDatabaseColumn(attribute: Quality?): String? = attribute?.value
    override fun convertToEntityAttribute(dbData: String?): Quality? = Quality.from(dbData)
}

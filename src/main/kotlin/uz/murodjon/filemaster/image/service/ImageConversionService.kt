package uz.murodjon.filemaster.image.service

import uz.murodjon.filemaster.auth.model.User
import uz.murodjon.filemaster.image.dto.ImageConvertRequest
import uz.murodjon.filemaster.conversion.dto.JobDto

interface ImageConversionService {
    fun convert(user: User, request: ImageConvertRequest): JobDto
}

package uz.murodjon.filemaster.document.service

import uz.murodjon.filemaster.auth.model.User
import uz.murodjon.filemaster.document.dto.DocumentConvertRequest
import uz.murodjon.filemaster.conversion.dto.JobDto

interface DocumentConversionService {
    fun convert(user: User, request: DocumentConvertRequest): JobDto
}
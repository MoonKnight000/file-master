package uz.murodjon.filemaster.archive.service

import uz.murodjon.filemaster.archive.dto.ArchiveConvertRequest
import uz.murodjon.filemaster.auth.model.User
import uz.murodjon.filemaster.conversion.dto.JobDto

interface ArchiveConversionService {
    fun convert(user: User, request: ArchiveConvertRequest): JobDto
}
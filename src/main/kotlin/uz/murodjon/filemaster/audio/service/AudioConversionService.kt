package uz.murodjon.filemaster.audio.service

import uz.murodjon.filemaster.auth.model.User
import uz.murodjon.filemaster.audio.dto.AudioConvertRequest
import uz.murodjon.filemaster.conversion.dto.JobDto

interface AudioConversionService {
    fun convert(user: User, request: AudioConvertRequest): JobDto
}

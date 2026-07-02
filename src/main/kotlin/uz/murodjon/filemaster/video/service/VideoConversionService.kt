package uz.murodjon.filemaster.video.service

import uz.murodjon.filemaster.auth.model.User
import uz.murodjon.filemaster.conversion.dto.JobDto
import uz.murodjon.filemaster.video.dto.VideoConvertRequest

interface VideoConversionService {
    fun convert(user: User, request: VideoConvertRequest): JobDto
}

package uz.murodjon.filemaster.video.controller

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController
import uz.murodjon.filemaster.auth.model.User
import uz.murodjon.filemaster.conversion.dto.JobDto
import uz.murodjon.filemaster.video.dto.VideoConvertRequest
import uz.murodjon.filemaster.video.service.VideoConversionService
import uz.murodjon.filemaster.tools.dto.ToolListResponse
import uz.murodjon.filemaster.tools.service.VideoToolService
import uz.murodjon.filemaster.util.ResponseData

@RestController
class VideoControllerImpl(
    private val videoTools: VideoToolService,
    private val videoConversion: VideoConversionService,
) : VideoController {

    override fun tools(): ResponseEntity<ResponseData<ToolListResponse>> =
        ResponseEntity.ok(ResponseData(videoTools.list()))

    override fun convert(
        request: VideoConvertRequest,
        user: User,
    ): ResponseEntity<ResponseData<JobDto>> =
        ResponseEntity.status(HttpStatus.ACCEPTED).body(ResponseData(videoConversion.convert(user, request)))
}
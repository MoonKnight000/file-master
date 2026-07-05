package uz.murodjon.filemaster.audio.controller

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController
import uz.murodjon.filemaster.auth.model.User
import uz.murodjon.filemaster.audio.dto.AudioConvertRequest
import uz.murodjon.filemaster.conversion.dto.JobDto
import uz.murodjon.filemaster.audio.service.AudioConversionService
import uz.murodjon.filemaster.tools.dto.ToolListResponse
import uz.murodjon.filemaster.audio.service.AudioToolService
import uz.murodjon.filemaster.util.ResponseData

@RestController
class AudioControllerImpl(
    private val audioTools: AudioToolService,
    private val audioConversion: AudioConversionService,
) : AudioController {

    override fun tools(): ResponseEntity<ResponseData<ToolListResponse>> {
        return ResponseEntity.ok(ResponseData(audioTools.list()))
    }

    override fun convert(
        request: AudioConvertRequest,
        user: User,
    ): ResponseEntity<ResponseData<JobDto>> {
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(ResponseData(audioConversion.convert(user, request)))
    }
}
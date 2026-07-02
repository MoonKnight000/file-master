package uz.murodjon.filemaster.image.controller

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController
import uz.murodjon.filemaster.auth.model.User
import uz.murodjon.filemaster.image.dto.ImageConvertRequest
import uz.murodjon.filemaster.conversion.dto.JobDto
import uz.murodjon.filemaster.image.service.ImageConversionService
import uz.murodjon.filemaster.tools.dto.ToolListResponse
import uz.murodjon.filemaster.tools.service.ImageToolService
import uz.murodjon.filemaster.util.ResponseData

@RestController
class ImageControllerImpl(
    private val imageTools: ImageToolService,
    private val imageConversion: ImageConversionService,
) : ImageController {

    override fun tools(): ResponseEntity<ResponseData<ToolListResponse>> =
        ResponseEntity.ok(ResponseData(imageTools.list()))

    override fun convert(
        request: ImageConvertRequest,
        user: User,
    ): ResponseEntity<ResponseData<JobDto>> =
        ResponseEntity.status(HttpStatus.ACCEPTED).body(ResponseData(imageConversion.convert(user, request)))
}
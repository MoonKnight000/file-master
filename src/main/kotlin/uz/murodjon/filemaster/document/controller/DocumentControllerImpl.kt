package uz.murodjon.filemaster.document.controller

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController
import uz.murodjon.filemaster.auth.model.User
import uz.murodjon.filemaster.document.dto.DocumentConvertRequest
import uz.murodjon.filemaster.conversion.dto.JobDto
import uz.murodjon.filemaster.document.service.DocumentConversionService
import uz.murodjon.filemaster.tools.dto.ToolListResponse
import uz.murodjon.filemaster.document.service.DocumentToolService
import uz.murodjon.filemaster.util.ResponseData

@RestController
class DocumentControllerImpl(
    private val documentTools: DocumentToolService,
    private val documentConversion: DocumentConversionService,
) : DocumentController {

    override fun tools(): ResponseEntity<ResponseData<ToolListResponse>> =
        ResponseEntity.ok(ResponseData(documentTools.list()))

    override fun convert(
        request: DocumentConvertRequest,
        user: User,
    ): ResponseEntity<ResponseData<JobDto>> =
        ResponseEntity.status(HttpStatus.ACCEPTED).body(ResponseData(documentConversion.convert(user, request)))
}
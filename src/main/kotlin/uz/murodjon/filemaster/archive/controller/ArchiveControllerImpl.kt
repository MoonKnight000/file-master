package uz.murodjon.filemaster.archive.controller

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController
import uz.murodjon.filemaster.archive.dto.ArchiveConvertRequest
import uz.murodjon.filemaster.auth.model.User
import uz.murodjon.filemaster.conversion.dto.JobDto
import uz.murodjon.filemaster.archive.service.ArchiveConversionService
import uz.murodjon.filemaster.tools.dto.ToolListResponse
import uz.murodjon.filemaster.archive.service.ArchiveToolService
import uz.murodjon.filemaster.util.ResponseData

@RestController
class ArchiveControllerImpl(
    private val archiveTools: ArchiveToolService,
    private val archiveConversion: ArchiveConversionService,
) : ArchiveController {

    override fun tools(): ResponseEntity<ResponseData<ToolListResponse>> =
        ResponseEntity.ok(ResponseData(archiveTools.list()))

    override fun convert(
        request: ArchiveConvertRequest,
        user: User,
    ): ResponseEntity<ResponseData<JobDto>> =
        ResponseEntity.status(HttpStatus.ACCEPTED).body(ResponseData(archiveConversion.convert(user, request)))
}
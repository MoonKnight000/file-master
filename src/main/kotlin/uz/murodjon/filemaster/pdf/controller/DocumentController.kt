package uz.murodjon.filemaster.pdf.controller

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import uz.murodjon.filemaster.auth.model.User
import uz.murodjon.filemaster.auth.security.CurrentUser
import uz.murodjon.filemaster.pdf.dto.DocumentConvertRequest
import uz.murodjon.filemaster.conversion.dto.JobDto
import uz.murodjon.filemaster.tools.dto.ToolListResponse
import uz.murodjon.filemaster.util.ResponseData

@RequestMapping("/v1/document")
interface DocumentController {

    @GetMapping("/tools")
    fun tools(): ResponseEntity<ResponseData<ToolListResponse>>

    @PostMapping("/conversions")
    fun convert(
        @RequestBody request: DocumentConvertRequest,
        @CurrentUser user: User,
    ): ResponseEntity<ResponseData<JobDto>>
}
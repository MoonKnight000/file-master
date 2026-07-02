package uz.murodjon.filemaster.image.controller

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import uz.murodjon.filemaster.auth.model.User
import uz.murodjon.filemaster.auth.security.CurrentUser
import uz.murodjon.filemaster.image.dto.ImageConvertRequest
import uz.murodjon.filemaster.conversion.dto.JobDto
import uz.murodjon.filemaster.tools.dto.ToolListResponse
import uz.murodjon.filemaster.util.ResponseData

@RequestMapping("/v1/image")
interface ImageController {

    @GetMapping("/tools")
    fun tools(): ResponseEntity<ResponseData<ToolListResponse>>

    @PostMapping("/conversions")
    fun convert(
        @RequestBody request: ImageConvertRequest,
        @CurrentUser user: User,
    ): ResponseEntity<ResponseData<JobDto>>
}
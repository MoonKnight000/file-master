package uz.murodjon.filemaster.conversion.controller

import jakarta.validation.Valid
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody
import uz.murodjon.filemaster.auth.model.User
import uz.murodjon.filemaster.auth.security.CurrentUser
import uz.murodjon.filemaster.conversion.dto.ConversionFilterRequest
import uz.murodjon.filemaster.conversion.dto.JobDto
import uz.murodjon.filemaster.util.PageableData
import uz.murodjon.filemaster.util.ResponseData

/**
 * Category-agnostic job endpoints (a job is the same regardless of category). Starting a
 * conversion lives on the per-category controllers (`/v1/{audio|video|image|document|archive}/conversions`).
 */
@RequestMapping("/v1/conversions")
interface ConversionController {

    @PostMapping("/filter")
    fun filter(
        @RequestBody @Valid filter: ConversionFilterRequest,
        @CurrentUser user: User,
    ): ResponseEntity<ResponseData<PageableData<JobDto>>>

    @GetMapping("/{jobId}")
    fun status(
        @PathVariable jobId: Long,
        @CurrentUser user: User,
    ): ResponseEntity<ResponseData<JobDto>>

    @GetMapping("/{jobId}/events", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun events(@PathVariable jobId: Long, @CurrentUser user: User): SseEmitter

    @GetMapping("/{jobId}/download")
    fun downloadAll(
        @PathVariable jobId: Long,
        @CurrentUser user: User,
    ): ResponseEntity<StreamingResponseBody>
}

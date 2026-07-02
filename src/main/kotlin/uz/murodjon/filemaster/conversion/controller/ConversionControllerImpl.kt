package uz.murodjon.filemaster.conversion.controller

import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody
import uz.murodjon.filemaster.auth.model.User
import uz.murodjon.filemaster.conversion.dto.ConversionFilterRequest
import uz.murodjon.filemaster.conversion.dto.JobDto
import uz.murodjon.filemaster.conversion.service.ConversionService
import uz.murodjon.filemaster.conversion.service.JobEvents
import uz.murodjon.filemaster.util.PageableData
import uz.murodjon.filemaster.util.ResponseData

@RestController
class ConversionControllerImpl(
    private val conversionService: ConversionService,
    private val events: JobEvents,
) : ConversionController {

    override fun filter(
        filter: ConversionFilterRequest,
        user: User,
    ): ResponseEntity<ResponseData<PageableData<JobDto>>> =
        ResponseEntity.ok(ResponseData(conversionService.filter(user, filter)))

    override fun status(
        jobId: Long,
        user: User,
    ): ResponseEntity<ResponseData<JobDto>> =
        ResponseEntity.ok(ResponseData(conversionService.status(user, jobId)))

    override fun events(jobId: Long, user: User): SseEmitter {
        val current = conversionService.status(user, jobId) // verifies ownership
        val emitter = events.subscribe(jobId)
        // Push the current state immediately so late subscribers aren't stuck blank.
        runCatching { emitter.send(SseEmitter.event().name("progress").data(current)) }
        return emitter
    }

    override fun downloadAll(
        jobId: Long,
        user: User,
    ): ResponseEntity<StreamingResponseBody> {
        val body = StreamingResponseBody { out -> conversionService.writeResultsZip(user, jobId, out) }
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"job-$jobId.zip\"")
            .contentType(MediaType.parseMediaType("application/zip"))
            .body(body)
    }
}
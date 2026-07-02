package uz.murodjon.filemaster.share.controller.impl

import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody
import uz.murodjon.filemaster.share.controller.ShareController
import uz.murodjon.filemaster.share.service.ShareService

@RestController
class ShareControllerImpl(private val shareService: ShareService) : ShareController {

    override fun download(token: String): ResponseEntity<StreamingResponseBody> {
        val file = shareService.resolve(token)
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"${file.name}\"")
            .contentType(MediaType.parseMediaType(file.contentType))
            .contentLength(file.bytes)
            .body(StreamingResponseBody { out -> file.writeTo(out) })
    }
}

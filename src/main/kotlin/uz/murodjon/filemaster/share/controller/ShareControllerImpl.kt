package uz.murodjon.filemaster.share.controller

import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody
import uz.murodjon.filemaster.auth.model.User
import uz.murodjon.filemaster.share.dto.ShareInfoResponse
import uz.murodjon.filemaster.share.service.ShareService
import uz.murodjon.filemaster.util.ResponseData

@RestController
class ShareControllerImpl(private val shareService: ShareService) : ShareController {

    override fun info(token: String): ResponseEntity<ResponseData<ShareInfoResponse>> =
        ResponseEntity.ok(ResponseData(shareService.info(token)))

    override fun download(token: String): ResponseEntity<StreamingResponseBody> {
        val file = shareService.resolve(token)
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"${file.name}\"")
            .contentType(MediaType.parseMediaType(file.contentType))
            .contentLength(file.bytes)
            .body(StreamingResponseBody { out -> file.writeTo(out) })
    }

    override fun revoke(token: String, user: User): ResponseEntity<ResponseData<Any>> {
        shareService.revoke(user, token)
        return ResponseEntity.ok(ResponseData(message = "Share link revoked"))
    }
}

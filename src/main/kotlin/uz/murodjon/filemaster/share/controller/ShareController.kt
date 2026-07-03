package uz.murodjon.filemaster.share.controller

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody
import uz.murodjon.filemaster.auth.model.User
import uz.murodjon.filemaster.auth.security.CurrentUser
import uz.murodjon.filemaster.share.dto.ShareInfoResponse
import uz.murodjon.filemaster.util.ResponseData

@RequestMapping("/v1/share")
interface ShareController {

    /** Public: share landing-page metadata (file name/size/type) — no auth. */
    @GetMapping("/{token}/info")
    fun info(@PathVariable token: String): ResponseEntity<ResponseData<ShareInfoResponse>>

    /** Public: download the shared file — no auth. */
    @GetMapping("/{token}")
    fun download(@PathVariable token: String): ResponseEntity<StreamingResponseBody>

    /** Owner-only: deactivate a share link. */
    @DeleteMapping("/{token}")
    fun revoke(
        @PathVariable token: String,
        @CurrentUser user: User,
    ): ResponseEntity<ResponseData<Any>>
}

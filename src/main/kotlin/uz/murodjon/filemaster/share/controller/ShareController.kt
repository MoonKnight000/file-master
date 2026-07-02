package uz.murodjon.filemaster.share.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody
import org.springframework.http.ResponseEntity

@RequestMapping("/v1/share")
interface ShareController {

    @GetMapping("/{token}")
    fun download(@PathVariable token: String): ResponseEntity<StreamingResponseBody>
}

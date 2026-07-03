package uz.murodjon.filemaster.tools.controller

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import uz.murodjon.filemaster.tools.dto.ToolDetail
import uz.murodjon.filemaster.tools.dto.ToolListResponse
import uz.murodjon.filemaster.util.ResponseData

@RequestMapping("/v1/tools")
interface ToolsController {

    @GetMapping
    fun list(@RequestParam(required = false) category: String?): ResponseEntity<ResponseData<ToolListResponse>>

    @GetMapping("/suggest")
    fun suggest(@RequestParam mime: String): ResponseEntity<ResponseData<ToolListResponse>>

    @GetMapping("/{slug}")
    fun detail(@PathVariable slug: String): ResponseEntity<ResponseData<ToolDetail>>
}

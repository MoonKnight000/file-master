package uz.murodjon.filemaster.tools.controller

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController
import uz.murodjon.filemaster.tools.dto.ToolDetail
import uz.murodjon.filemaster.tools.dto.ToolListResponse
import uz.murodjon.filemaster.tools.service.ToolsService
import uz.murodjon.filemaster.util.ResponseData

@RestController
class ToolsControllerImpl(
    private val toolsService: ToolsService,
) : ToolsController {

    override fun list(category: String?): ResponseEntity<ResponseData<ToolListResponse>> =
        ResponseEntity.ok(ResponseData(toolsService.list(category)))

    override fun suggest(mime: String): ResponseEntity<ResponseData<ToolListResponse>> =
        ResponseEntity.ok(ResponseData(toolsService.suggest(mime)))

    override fun detail(slug: String): ResponseEntity<ResponseData<ToolDetail>> =
        ResponseEntity.ok(ResponseData(toolsService.detail(slug)))
}

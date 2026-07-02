package uz.murodjon.filemaster.tools.controller.impl

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController
import uz.murodjon.filemaster.auth.model.User
import uz.murodjon.filemaster.tools.controller.ToolsController
import uz.murodjon.filemaster.tools.dto.FavoriteToggleResponse
import uz.murodjon.filemaster.tools.dto.ToolDetail
import uz.murodjon.filemaster.tools.dto.ToolListResponse
import uz.murodjon.filemaster.tools.dto.UserToolsResponse
import uz.murodjon.filemaster.tools.service.ToolsService
import uz.murodjon.filemaster.tools.service.UserToolStatService
import uz.murodjon.filemaster.util.ResponseData

@RestController
class ToolsControllerImpl(
    private val toolsService: ToolsService,
    private val userToolStatService: UserToolStatService
) : ToolsController {

    override fun list(category: String?): ResponseEntity<ResponseData<ToolListResponse>> =
        ResponseEntity.ok(ResponseData(toolsService.list(category)))

    override fun suggest(mime: String): ResponseEntity<ResponseData<ToolListResponse>> =
        ResponseEntity.ok(ResponseData(toolsService.suggest(mime)))

    override fun detail(slug: String): ResponseEntity<ResponseData<ToolDetail>> =
        ResponseEntity.ok(ResponseData(toolsService.detail(slug)))

    override fun myTools(user: User): ResponseEntity<ResponseData<UserToolsResponse>> =
        ResponseEntity.ok(ResponseData(userToolStatService.myTools(user)))

    override fun toggleFavorite(
        slug: String,
        user: User,
    ): ResponseEntity<ResponseData<FavoriteToggleResponse>> =
        ResponseEntity.ok(ResponseData(userToolStatService.toggleFavorite(user, slug)))
}

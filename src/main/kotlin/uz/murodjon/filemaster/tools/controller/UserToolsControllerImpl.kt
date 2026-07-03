package uz.murodjon.filemaster.tools.controller

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController
import uz.murodjon.filemaster.auth.model.User
import uz.murodjon.filemaster.tools.dto.FavoriteToggleResponse
import uz.murodjon.filemaster.tools.dto.UserToolsResponse
import uz.murodjon.filemaster.tools.service.UserToolStatService
import uz.murodjon.filemaster.util.ResponseData

@RestController
class UserToolsControllerImpl(
    private val userToolStatService: UserToolStatService,
) : UserToolsController {

    override fun myTools(user: User): ResponseEntity<ResponseData<UserToolsResponse>> =
        ResponseEntity.ok(ResponseData(userToolStatService.myTools(user)))

    override fun toggleFavorite(
        slug: String,
        user: User,
    ): ResponseEntity<ResponseData<FavoriteToggleResponse>> =
        ResponseEntity.ok(ResponseData(userToolStatService.toggleFavorite(user, slug)))
}

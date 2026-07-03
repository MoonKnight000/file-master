package uz.murodjon.filemaster.tools.controller

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import uz.murodjon.filemaster.auth.model.User
import uz.murodjon.filemaster.auth.security.CurrentUser
import uz.murodjon.filemaster.tools.dto.FavoriteToggleResponse
import uz.murodjon.filemaster.tools.dto.UserToolsResponse
import uz.murodjon.filemaster.util.ResponseData

/** The current user's personal tool state (favorites + recents) — hence `/v1/me/...`. */
@RequestMapping("/v1/me/tools")
interface UserToolsController {

    @GetMapping
    fun myTools(@CurrentUser user: User): ResponseEntity<ResponseData<UserToolsResponse>>

    @PostMapping("/{slug}/favorite")
    fun toggleFavorite(
        @PathVariable slug: String,
        @CurrentUser user: User,
    ): ResponseEntity<ResponseData<FavoriteToggleResponse>>
}

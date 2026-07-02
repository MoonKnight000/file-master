package uz.murodjon.filemaster.tools.controller

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import uz.murodjon.filemaster.auth.model.User
import uz.murodjon.filemaster.auth.security.CurrentUser
import uz.murodjon.filemaster.tools.dto.FavoriteToggleResponse
import uz.murodjon.filemaster.tools.dto.ToolDetail
import uz.murodjon.filemaster.tools.dto.ToolListResponse
import uz.murodjon.filemaster.tools.dto.UserToolsResponse
import uz.murodjon.filemaster.util.ResponseData

@RequestMapping("/v1/tools")
interface ToolsController {

    @GetMapping
    fun list(@RequestParam(required = false) category: String?): ResponseEntity<ResponseData<ToolListResponse>>

    @GetMapping("/suggest")
    fun suggest(@RequestParam mime: String): ResponseEntity<ResponseData<ToolListResponse>>

    @GetMapping("/{slug}")
    fun detail(@PathVariable slug: String): ResponseEntity<ResponseData<ToolDetail>>

    @GetMapping("/tools")
    fun myTools(@CurrentUser user: User): ResponseEntity<ResponseData<UserToolsResponse>>

    @PostMapping("/tools/{slug}/favorite")
    fun toggleFavorite(
        @PathVariable slug: String,
        @CurrentUser user: User,
    ): ResponseEntity<ResponseData<FavoriteToggleResponse>>
}

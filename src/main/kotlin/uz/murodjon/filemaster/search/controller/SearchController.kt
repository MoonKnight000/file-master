package uz.murodjon.filemaster.search.controller

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import uz.murodjon.filemaster.auth.model.User
import uz.murodjon.filemaster.auth.security.CurrentUser
import uz.murodjon.filemaster.search.dto.SearchResponse
import uz.murodjon.filemaster.util.ResponseData

@RequestMapping("/v1/search")
interface SearchController {

    @GetMapping
    fun search(
        @RequestParam(required = false, defaultValue = "") q: String,
        @RequestParam(required = false, defaultValue = "10") limit: Int,
        @CurrentUser user: User,
    ): ResponseEntity<ResponseData<SearchResponse>>
}

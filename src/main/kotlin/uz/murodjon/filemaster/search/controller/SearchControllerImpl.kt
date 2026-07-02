package uz.murodjon.filemaster.search.controller

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController
import uz.murodjon.filemaster.auth.model.User
import uz.murodjon.filemaster.search.dto.SearchResponse
import uz.murodjon.filemaster.search.service.SearchService
import uz.murodjon.filemaster.util.ResponseData

@RestController
class SearchControllerImpl(
    private val searchService: SearchService,
) : SearchController {

    override fun search(
        q: String,
        limit: Int,
        user: User,
    ): ResponseEntity<ResponseData<SearchResponse>> =
        ResponseEntity.ok(ResponseData(searchService.search(user, q, limit)))
}
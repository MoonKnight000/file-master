package uz.murodjon.filemaster.search.service

import uz.murodjon.filemaster.auth.model.User
import uz.murodjon.filemaster.search.dto.SearchResponse

interface SearchService {
    fun search(user: User, q: String, limit: Int): SearchResponse
}

package uz.murodjon.filemaster.search.service.impl

import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uz.murodjon.filemaster.auth.model.User
import uz.murodjon.filemaster.files.repository.StoredFileRepository
import uz.murodjon.filemaster.search.dto.SearchFileDto
import uz.murodjon.filemaster.search.dto.SearchResponse
import uz.murodjon.filemaster.search.dto.SearchToolDto
import uz.murodjon.filemaster.search.service.SearchService
import uz.murodjon.filemaster.tools.service.ToolProvider

@Service
class SearchServiceImpl(
    private val tools: ToolProvider,
    private val files: StoredFileRepository,
) : SearchService {

    @Transactional(readOnly = true)
    override fun search(user: User, q: String, limit: Int): SearchResponse {
        val query = q.trim()
        val cap = limit.coerceIn(1, 50)
        if (query.isBlank()) return SearchResponse(emptyList(), emptyList())

        val matchedTools = tools.all()
            .filter {
                it.title.contains(query, true) || it.slug.contains(query, true) || it.desc.contains(query, true)
            }
            .take(cap)
            .map { SearchToolDto(it.slug, it.title, it.category, it.icon) }

        val matchedFiles = files
            .findByUserIdAndActiveTrueAndOriginalNameContainingIgnoreCase(user.id!!, query, PageRequest.of(0, cap))
            .map { SearchFileDto(it.id!!, it.originalName, it.category, "/v1/files/${it.id}/download") }

        return SearchResponse(matchedTools, matchedFiles)
    }
}

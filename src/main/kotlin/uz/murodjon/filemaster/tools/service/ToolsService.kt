package uz.murodjon.filemaster.tools.service

import uz.murodjon.filemaster.tools.dto.ToolDetail
import uz.murodjon.filemaster.tools.dto.ToolListResponse

/**
 * Cross-cutting catalog browsing (all tools / by category / one tool's detail). Per-category
 * listings for the audio/video/image/document/archive controllers live in their own
 * `XToolService` instead.
 */
interface ToolsService {
    fun list(category: String?): ToolListResponse
    fun detail(slug: String): ToolDetail
    fun suggest(mime: String): ToolListResponse
}

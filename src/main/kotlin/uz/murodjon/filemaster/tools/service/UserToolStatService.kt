package uz.murodjon.filemaster.tools.service

import uz.murodjon.filemaster.auth.model.User
import uz.murodjon.filemaster.tools.dto.FavoriteToggleResponse
import uz.murodjon.filemaster.tools.dto.UserToolsResponse

interface UserToolStatService {
    fun myTools(user: User): UserToolsResponse
    fun toggleFavorite(user: User, slug: String): FavoriteToggleResponse
    fun recordUsage(userId: Long, toolSlug: String)
}

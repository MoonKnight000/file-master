package uz.murodjon.filemaster.tools.service.impl

import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uz.murodjon.filemaster.auth.model.User
import uz.murodjon.filemaster.exception.ToolNotFoundException
import uz.murodjon.filemaster.tools.dto.FavoriteToggleResponse
import uz.murodjon.filemaster.tools.dto.UserToolsResponse
import uz.murodjon.filemaster.tools.model.UserToolStat
import uz.murodjon.filemaster.tools.repository.UserToolStatRepository
import uz.murodjon.filemaster.tools.service.ToolProvider
import uz.murodjon.filemaster.tools.service.UserToolStatService
import java.time.Instant

@Service
class UserToolStatServiceImpl(
    private val stats: UserToolStatRepository,
    private val toolProvider: ToolProvider,
    @PersistenceContext private val em: EntityManager,
) : UserToolStatService {

    override fun myTools(user: User): UserToolsResponse {
        val userId = user.id!!
        val favoriteSlugs = stats.findByUserIdAndFavoritedTrue(userId).map { it.toolSlug }.toSet()

        val favorites = favoriteSlugs.mapNotNull { toolProvider.findBySlug(it) }

        val recentStats = stats.findByUserIdAndLastUsedTimestampNotNullOrderByLastUsedTimestampDesc(
            userId, PageRequest.of(0, 20),
        )
        val recent = recentStats
            .filter { it.toolSlug !in favoriteSlugs }
            .mapNotNull { toolProvider.findBySlug(it.toolSlug) }
            .take(10)

        return UserToolsResponse(favorites = favorites, recent = recent)
    }

    @Transactional
    override fun toggleFavorite(user: User, slug: String): FavoriteToggleResponse {
        toolProvider.findBySlug(slug) ?: throw ToolNotFoundException(slug)
        val stat = findOrCreate(user.id!!, slug)
        stat.favorited = !stat.favorited
        stat.updatedTimestamp = Instant.now().epochSecond
        stats.save(stat)
        return FavoriteToggleResponse(slug = slug, favorited = stat.favorited)
    }

    @Transactional
    override fun recordUsage(userId: Long, toolSlug: String) {
        val stat = findOrCreate(userId, toolSlug)
        stat.useCount++
        stat.lastUsedTimestamp = Instant.now().epochSecond
        stat.updatedTimestamp = Instant.now().epochSecond
        stats.save(stat)
    }

    private fun findOrCreate(userId: Long, toolSlug: String): UserToolStat =
        stats.findByUserIdAndToolSlug(userId, toolSlug).orElseGet {
            UserToolStat(
                user = em.getReference(User::class.java, userId),
                toolSlug = toolSlug,
            )
        }
}

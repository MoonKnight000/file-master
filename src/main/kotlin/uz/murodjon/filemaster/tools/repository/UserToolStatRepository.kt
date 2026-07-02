package uz.murodjon.filemaster.tools.repository

import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import uz.murodjon.filemaster.tools.model.UserToolStat
import java.util.Optional

@Repository
interface UserToolStatRepository : JpaRepository<UserToolStat, Long> {
    fun findByUserIdAndToolSlug(userId: Long, toolSlug: String): Optional<UserToolStat>
    fun findByUserIdAndFavoritedTrue(userId: Long): List<UserToolStat>
    fun findByUserIdAndLastUsedTimestampNotNullOrderByLastUsedTimestampDesc(
        userId: Long,
        pageable: Pageable,
    ): List<UserToolStat>
}

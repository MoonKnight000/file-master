package uz.murodjon.filemaster.share.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import uz.murodjon.filemaster.share.model.ShareLink
import java.util.Optional

@Repository
interface ShareLinkRepository : JpaRepository<ShareLink, Long> {
    fun findByTokenAndActiveTrue(token: String): Optional<ShareLink>
}

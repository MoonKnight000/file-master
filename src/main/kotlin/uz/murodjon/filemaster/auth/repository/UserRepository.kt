package uz.murodjon.filemaster.auth.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import uz.murodjon.filemaster.auth.enums.UserPlan
import uz.murodjon.filemaster.auth.model.User
import java.util.Optional

@Repository
interface UserRepository : JpaRepository<User, Long> {
    fun findByEmailIgnoreCaseAndActiveTrue(email: String): Optional<User>

    fun findByGoogleIdAndActiveTrue(googleId: String): Optional<User>

    /** Paid accounts whose period has lapsed — swept back to FREE by the billing schedule. */
    fun findByPlanAndPlanExpiresTimestampLessThanAndActiveTrue(
        plan: UserPlan,
        cutoff: Long,
    ): List<User>
}

package uz.murodjon.filemaster.billing.schedule

import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uz.murodjon.filemaster.auth.enums.UserPlan
import uz.murodjon.filemaster.auth.repository.UserRepository
import java.time.Instant

/** Lapses paid plans whose period ended back to FREE (hourly, 2 min after startup). */
@Service
class PlanExpirySchedule(private val users: UserRepository) {

    private val log = LoggerFactory.getLogger(javaClass)

    @Scheduled(fixedDelay = 60 * 60 * 1000, initialDelay = 2 * 60 * 1000)
    @Transactional
    fun downgradeExpiredPlans() {
        val now = Instant.now().epochSecond
        val expired = users.findByPlanAndPlanExpiresTimestampLessThanAndActiveTrue(UserPlan.PREMIUM, now)
        if (expired.isEmpty()) return

        expired.forEach {
            it.plan = UserPlan.FREE
            it.planExpiresTimestamp = null
            it.updatedTimestamp = now
        }
        users.saveAll(expired)
        log.info("Billing: downgraded {} expired PREMIUM account(s) to FREE.", expired.size)
    }
}

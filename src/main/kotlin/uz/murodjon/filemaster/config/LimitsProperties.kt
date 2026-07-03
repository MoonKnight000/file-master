package uz.murodjon.filemaster.config

import uz.murodjon.filemaster.auth.enums.UserPlan

data class LimitsProperties(
    val maxFileBytes: Long,
    val retentionMinutes: Long,
    val freeBatchConvert: Boolean,
    /** Result retention for PREMIUM users (free users get [retentionMinutes]). */
    val retentionMinutesPremium: Long = 7 * 24 * 60,
    /** Rolling-24h conversion quota for guests; <= 0 disables the check. */
    val dailyConversionsGuest: Int = 10,
    /** Rolling-24h conversion quota for registered FREE users; <= 0 disables the check. */
    val dailyConversionsFree: Int = 30,
) {

    fun retentionMinutesFor(plan: UserPlan): Long =
        if (plan == UserPlan.PREMIUM) retentionMinutesPremium else retentionMinutes

    /** Daily quota for a user, or null when unlimited (PREMIUM / disabled quota). */
    fun dailyConversionLimit(guest: Boolean, plan: UserPlan): Int? {
        if (plan == UserPlan.PREMIUM) return null
        val limit = if (guest) dailyConversionsGuest else dailyConversionsFree
        return limit.takeIf { it > 0 }
    }
}

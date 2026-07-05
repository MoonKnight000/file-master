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
    /** Max input files in one conversion job for FREE/guest users (when batch is allowed at all). */
    val maxBatchFilesFree: Int = 5,
    /** Max input files in one conversion job for PREMIUM users. */
    val maxBatchFiles: Int = 20,
    /** Upload (source file) retention for FREE/guest users; uploads referenced by a running job are kept. */
    val uploadRetentionMinutes: Long = 24 * 60,
    /** Upload retention for PREMIUM users. */
    val uploadRetentionMinutesPremium: Long = 30 * 24 * 60,
    /** A QUEUED/PROCESSING job older than this is considered dead and failed by the reaper. */
    val staleJobMinutes: Long = 60,
) {

    fun retentionMinutesFor(plan: UserPlan): Long =
        if (plan == UserPlan.PREMIUM) retentionMinutesPremium else retentionMinutes

    fun uploadRetentionMinutesFor(plan: UserPlan): Long =
        if (plan == UserPlan.PREMIUM) uploadRetentionMinutesPremium else uploadRetentionMinutes

    fun maxBatchFilesFor(plan: UserPlan): Int =
        if (plan == UserPlan.PREMIUM) maxBatchFiles else maxBatchFilesFree

    /** Daily quota for a user, or null when unlimited (PREMIUM / disabled quota). */
    fun dailyConversionLimit(guest: Boolean, plan: UserPlan): Int? {
        if (plan == UserPlan.PREMIUM) return null
        val limit = if (guest) dailyConversionsGuest else dailyConversionsFree
        return limit.takeIf { it > 0 }
    }
}

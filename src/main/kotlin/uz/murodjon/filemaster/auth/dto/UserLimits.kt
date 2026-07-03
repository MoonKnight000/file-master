package uz.murodjon.filemaster.auth.dto

/**
 * Effective limits for the current user's plan.
 * [dailyConversions] is null when unlimited (PREMIUM or quota disabled).
 */
data class UserLimits(
    val maxFileBytes: Long,
    val batchConvert: Boolean,
    val retentionMinutes: Long,
    val dailyConversions: Int? = null,
)

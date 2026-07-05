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
    /** Max input files in one conversion job. */
    val maxBatchFiles: Int,
    /** How long uploaded source files are kept (results follow [retentionMinutes]). */
    val uploadRetentionMinutes: Long,
)

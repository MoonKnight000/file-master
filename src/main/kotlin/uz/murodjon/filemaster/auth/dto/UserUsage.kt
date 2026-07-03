package uz.murodjon.filemaster.auth.dto

/**
 * Account usage snapshot for the "Account & usage" page.
 * - [fileCount]: all of the user's active files (also drives the sidebar "My files" badge).
 * - [storageBytes]: total size of those files.
 * - [conversionCount]: how many conversion jobs the user has started (all time).
 * - [conversionsToday]: jobs started in the last 24h — pair with `limits.dailyConversions`
 *   for the "N of M used today" meter.
 * - [byCategory]: per-category file-count breakdown.
 */
data class UserUsage(
    val fileCount: Long,
    val storageBytes: Long,
    val conversionCount: Long,
    val conversionsToday: Long,
    val byCategory: List<CategoryUsage>,
)

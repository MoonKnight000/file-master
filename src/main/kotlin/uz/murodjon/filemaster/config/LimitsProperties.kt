package uz.murodjon.filemaster.config

data class LimitsProperties(
    val maxFileBytes: Long,
    val retentionMinutes: Long,
    val freeBatchConvert: Boolean,
)

package uz.murodjon.filemaster.auth.dto

data class UserLimits(val maxFileBytes: Long, val batchConvert: Boolean, val retentionMinutes: Long)

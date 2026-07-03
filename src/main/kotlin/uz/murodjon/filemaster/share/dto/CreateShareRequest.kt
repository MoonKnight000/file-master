package uz.murodjon.filemaster.share.dto

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min

data class CreateShareRequest(
    @field:Min(1, message = "ttlHours must be at least 1")
    @field:Max(168, message = "ttlHours must be at most 168 (7 days)")
    val ttlHours: Int = 24,
)

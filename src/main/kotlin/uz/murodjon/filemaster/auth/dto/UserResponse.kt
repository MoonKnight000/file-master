package uz.murodjon.filemaster.auth.dto

import uz.murodjon.filemaster.auth.enums.UserPlan
import uz.murodjon.filemaster.files.dto.FileDto

data class UserResponse(
    val id: Long,
    val guest: Boolean,
    val name: String?,
    val email: String?,
    val avatar: FileDto?,
    val plan: UserPlan,
    val limits: UserLimits,
    val usage: UserUsage,
)

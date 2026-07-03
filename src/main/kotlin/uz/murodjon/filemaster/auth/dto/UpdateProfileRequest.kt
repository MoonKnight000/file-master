package uz.murodjon.filemaster.auth.dto

import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

data class UpdateProfileRequest(
    @field:Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    @field:Pattern(regexp = ".*\\S.*", message = "Name must not be blank")
    val name: String? = null,
    val avatarId: Long? = null,
)

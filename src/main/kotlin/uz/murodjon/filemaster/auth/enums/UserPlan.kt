package uz.murodjon.filemaster.auth.enums

import com.fasterxml.jackson.annotation.JsonValue

enum class UserPlan(@get:JsonValue val value: String) {
    FREE("free"),
    PREMIUM("premium"),
}

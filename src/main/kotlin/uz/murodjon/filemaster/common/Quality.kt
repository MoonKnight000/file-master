package uz.murodjon.filemaster.common

import com.fasterxml.jackson.annotation.JsonValue

enum class Quality(@get:JsonValue val value: String) {
    HIGH("high"),
    BALANCED("balanced"),
    SMALL("small");

    companion object {
        fun from(value: String?): Quality =
            entries.firstOrNull { it.value.equals(value, ignoreCase = true) } ?: BALANCED
    }
}

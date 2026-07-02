package uz.murodjon.filemaster.tools.enums

import com.fasterxml.jackson.annotation.JsonValue

/** Small promo label shown on a tool tile. Null = no badge. */
enum class ToolBadge(@get:JsonValue val value: String) {
    POPULAR("Popular"),
    NEW("New"),
}

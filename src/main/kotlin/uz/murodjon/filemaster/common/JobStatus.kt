package uz.murodjon.filemaster.common

import com.fasterxml.jackson.annotation.JsonValue

enum class JobStatus(@get:JsonValue val value: String) {
    QUEUED("queued"),
    PROCESSING("processing"),
    DONE("done"),
    FAILED("failed");
}

package uz.murodjon.filemaster.util

/** Standard success envelope for every JSON endpoint (binary/SSE excluded). */
data class ResponseData<T>(
    val data: T? = null,
    val message: String = "Success",
    val success: Boolean = true,
)

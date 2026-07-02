package uz.murodjon.filemaster.exception

data class ErrorBody(val code: String, val message: String, val details: Map<String, Any?>? = null)

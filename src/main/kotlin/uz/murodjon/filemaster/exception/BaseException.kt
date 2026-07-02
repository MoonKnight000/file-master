package uz.murodjon.filemaster.exception

import org.springframework.http.HttpStatus

/**
 * Base class for every domain exception. A concrete exception only needs to
 * declare its [code]; the HTTP [status] is derived from the code by default but
 * can be overridden. Optional [details] are surfaced in the error envelope.
 */
abstract class BaseException(message: String? = null) : RuntimeException(message) {
    abstract val code: ExcCode
    open val status: HttpStatus get() = code.status
    open val details: Map<String, Any?>? get() = null
}

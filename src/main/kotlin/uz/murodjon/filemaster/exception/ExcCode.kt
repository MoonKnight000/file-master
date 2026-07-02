package uz.murodjon.filemaster.exception

import org.springframework.http.HttpStatus

/**
 * Application error codes. Each carries a stable numeric code and the HTTP
 * status it maps to. The enum *name* is what the front-end sees in the error
 * envelope (e.g. `"FILE_TOO_LARGE"`), matching docs/API.md.
 */
enum class ExcCode(val code: Int, val status: HttpStatus) {
    VALIDATION_ERROR(100, HttpStatus.BAD_REQUEST),
    UNSUPPORTED_FORMAT(101, HttpStatus.BAD_REQUEST),
    UNAUTHENTICATED(102, HttpStatus.UNAUTHORIZED),
    FORBIDDEN(103, HttpStatus.FORBIDDEN),
    PLAN_LIMIT(104, HttpStatus.FORBIDDEN),
    NOT_FOUND(105, HttpStatus.NOT_FOUND),
    FILE_TOO_LARGE(106, HttpStatus.PAYLOAD_TOO_LARGE),
    CONVERSION_FAILED(107, HttpStatus.UNPROCESSABLE_ENTITY),
    RATE_LIMITED(108, HttpStatus.TOO_MANY_REQUESTS),
    INTERNAL(109, HttpStatus.INTERNAL_SERVER_ERROR),
}

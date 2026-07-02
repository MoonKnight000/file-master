package uz.murodjon.filemaster.exception

import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.multipart.MaxUploadSizeExceededException
import org.springframework.web.multipart.MultipartException

@RestControllerAdvice
class GlobalExceptionHandler {

    private val log = LoggerFactory.getLogger(javaClass)

    @ExceptionHandler(BaseException::class)
    fun handleBase(ex: BaseException): ResponseEntity<ErrorResponse> =
        ResponseEntity.status(ex.status)
            .body(ErrorResponse(ErrorBody(ex.code.name, ex.message ?: ex.code.name, ex.details)))

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(ex: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
        val msg = ex.bindingResult.fieldErrors.joinToString("; ") { "${it.field}: ${it.defaultMessage}" }
        return ResponseEntity.status(ExcCode.VALIDATION_ERROR.status)
            .body(ErrorResponse(ErrorBody(ExcCode.VALIDATION_ERROR.name, msg.ifBlank { "Invalid request." })))
    }

    @ExceptionHandler(MaxUploadSizeExceededException::class)
    fun handleTooLarge(ex: MaxUploadSizeExceededException): ResponseEntity<ErrorResponse> =
        ResponseEntity.status(ExcCode.FILE_TOO_LARGE.status)
            .body(ErrorResponse(ErrorBody(ExcCode.FILE_TOO_LARGE.name, "Upload exceeds the size limit.")))

    /**
     * Multipart parse failures other than size — most often the client/proxy aborting a
     * slow upload mid-stream (`ClientAbortException`/EOF). Report a clean 400 instead of a
     * 500 stack trace; the connection is usually already gone so this is best-effort.
     */
    @ExceptionHandler(MultipartException::class)
    fun handleMultipart(ex: MultipartException): ResponseEntity<ErrorResponse> {
        log.warn("Multipart upload failed (client may have disconnected): {}", ex.message)
        return ResponseEntity.status(ExcCode.VALIDATION_ERROR.status)
            .body(ErrorResponse(ErrorBody(ExcCode.VALIDATION_ERROR.name, "Upload failed or was interrupted. Please retry.")))
    }

    @ExceptionHandler(Exception::class)
    fun handleOther(ex: Exception): ResponseEntity<ErrorResponse> {
        log.error("Unhandled error", ex)
        return ResponseEntity.status(ExcCode.INTERNAL.status)
            .body(ErrorResponse(ErrorBody(ExcCode.INTERNAL.name, "Server error.")))
    }
}

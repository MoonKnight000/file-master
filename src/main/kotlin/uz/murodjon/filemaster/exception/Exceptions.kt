package uz.murodjon.filemaster.exception


// --- 400 VALIDATION_ERROR ---
class ValidationException(
    message: String,
    override val details: Map<String, Any?>? = null,
) : BaseException(message) {
    override val code = ExcCode.VALIDATION_ERROR
}

// --- 400 UNSUPPORTED_FORMAT ---
class UnsupportedFormatException(
    message: String,
    override val details: Map<String, Any?>? = null,
) : BaseException(message) {
    override val code = ExcCode.UNSUPPORTED_FORMAT
}

// --- 401 UNAUTHENTICATED ---
class UnauthenticatedException(
    message: String = "Missing or invalid token.",
) : BaseException(message) {
    override val code = ExcCode.UNAUTHENTICATED
}

// --- 403 FORBIDDEN ---
class ForbiddenException(
    message: String = "Forbidden.",
) : BaseException(message) {
    override val code = ExcCode.FORBIDDEN
}

// --- 403 PLAN_LIMIT ---
class PlanLimitException(
    message: String,
    override val details: Map<String, Any?>? = null,
) : BaseException(message) {
    override val code = ExcCode.PLAN_LIMIT
}

// --- 404 NOT_FOUND ---
class NotFoundException(
    message: String = "Not found.",
) : BaseException(message) {
    override val code = ExcCode.NOT_FOUND
}

class ToolNotFoundException(slug: String) : BaseException("Unknown tool: $slug") {
    override val code = ExcCode.NOT_FOUND
}

class UploadNotFoundException(
    message: String = "Upload not found.",
) : BaseException(message) {
    override val code = ExcCode.NOT_FOUND
}

class JobNotFoundException(
    message: String = "Job not found.",
) : BaseException(message) {
    override val code = ExcCode.NOT_FOUND
}

class ResultFileNotFoundException(
    message: String = "File not found.",
) : BaseException(message) {
    override val code = ExcCode.NOT_FOUND
}

class AvatarFileNotFoundException(
    message: String = "Avatar file not found.",
) : BaseException(message) {
    override val code = ExcCode.NOT_FOUND
}

// --- 413 FILE_TOO_LARGE ---
class FileTooLargeException(limitBytes: Long) :
    BaseException("Files must be ${limitBytes / (1024 * 1024)} MB or smaller.") {
    override val code = ExcCode.FILE_TOO_LARGE
    override val details = mapOf<String, Any?>("limitBytes" to limitBytes)
}

// --- 422 CONVERSION_FAILED ---
class ConversionFailedException(
    message: String = "Job could not be processed.",
) : BaseException(message) {
    override val code = ExcCode.CONVERSION_FAILED
}

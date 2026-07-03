package uz.murodjon.filemaster.mail.service

/**
 * Outbound e-mail. The active implementation is a structured log stub until SMTP
 * credentials are configured — swap in an SMTP-backed impl without touching callers.
 */
interface MailService {

    /** "Your conversion is ready" notification for a finished job. */
    fun sendConversionDone(email: String, jobId: Long, fileNames: List<String>)
}

package uz.murodjon.filemaster.mail.service.impl

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uz.murodjon.filemaster.mail.service.MailService

/**
 * No-SMTP stand-in: logs what WOULD be sent. Replace with a `spring-boot-starter-mail`
 * backed impl (JavaMailSender) once an SMTP account exists; the interface stays the same.
 */
@Service
class LogMailService : MailService {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun sendConversionDone(email: String, jobId: Long, fileNames: List<String>) {
        log.info(
            "MAIL (stub) -> {}: conversion job {} done, {} file(s): {}",
            email, jobId, fileNames.size, fileNames.take(5).joinToString(),
        )
    }
}

package uz.murodjon.filemaster.conversion.engine

import org.apache.pdfbox.Loader
import org.apache.pdfbox.pdmodel.encryption.AccessPermission
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException
import org.apache.pdfbox.pdmodel.encryption.StandardProtectionPolicy
import org.springframework.stereotype.Component
import uz.murodjon.filemaster.exception.ConversionFailedException
import java.nio.file.Path

/**
 * Password-protects and unlocks PDFs via Apache PDFBox (256-bit AES, needs BouncyCastle).
 * Error messages must never contain the password — they flow into job errors and logs.
 */
@Component
class PdfSecurity {

    /** Encrypts [input] with [password] (used as both user and owner password), writing [output]. */
    fun protect(input: Path, password: String, output: Path) {
        Loader.loadPDF(input.toFile()).use { document ->
            val policy = StandardProtectionPolicy(password, password, AccessPermission()).apply {
                encryptionKeyLength = 256
            }
            document.protect(policy)
            document.save(output.toFile())
        }
    }

    /**
     * True when the PDF opens with [password] (a non-encrypted PDF always does). Used for the
     * cheap submit-time password check on unlock-pdf. Non-password load failures (corrupt
     * file, not a PDF) return true — those are the conversion job's problem, not the password's.
     */
    fun passwordAccepted(input: Path, password: String?): Boolean = try {
        Loader.loadPDF(input.toFile(), password ?: "").close()
        true
    } catch (e: InvalidPasswordException) {
        false
    } catch (e: Exception) {
        true
    }

    /**
     * Removes the encryption from [input] using [password] to open it, writing [output].
     * A PDF that is not encrypted passes through unchanged.
     */
    fun unlock(input: Path, password: String?, output: Path) {
        val document = try {
            Loader.loadPDF(input.toFile(), password ?: "")
        } catch (e: InvalidPasswordException) {
            throw ConversionFailedException("Wrong or missing password for this protected PDF.")
        }
        document.use {
            it.isAllSecurityToBeRemoved = true
            it.save(output.toFile())
        }
    }
}

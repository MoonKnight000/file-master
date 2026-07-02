package uz.murodjon.filemaster.conversion.engine

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import uz.murodjon.filemaster.exception.ConversionFailedException
import java.util.concurrent.TimeUnit

@Component
class ProcessRunner {
    private val log = LoggerFactory.getLogger(javaClass)

    /** Runs an external command, capturing combined output. Throws on non-zero exit or timeout. */
    fun run(command: List<String>, timeoutSeconds: Long): String {
        log.info("exec: {}", command.joinToString(" "))
        val process = ProcessBuilder(command)
            .redirectErrorStream(true)
            .start()

        val output = process.inputStream.bufferedReader().readText()
        val finished = process.waitFor(timeoutSeconds, TimeUnit.SECONDS)
        if (!finished) {
            process.destroyForcibly()
            throw ConversionFailedException("Conversion timed out after ${timeoutSeconds}s.")
        }
        if (process.exitValue() != 0) {
            log.warn("Command failed ({}): {}", process.exitValue(), output.take(2000))
            throw ConversionFailedException("Conversion tool exited with code ${process.exitValue()}.")
        }
        return output
    }
}

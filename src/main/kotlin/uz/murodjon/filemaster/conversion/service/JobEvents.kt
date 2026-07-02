package uz.murodjon.filemaster.conversion.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

/** In-memory fan-out of conversion progress to connected SSE clients. */
@Component
class JobEvents {
    private val log = LoggerFactory.getLogger(javaClass)
    private val emitters = ConcurrentHashMap<Long, CopyOnWriteArrayList<SseEmitter>>()

    fun subscribe(jobId: Long): SseEmitter {
        val emitter = SseEmitter(0L) // no timeout
        val list = emitters.computeIfAbsent(jobId) { CopyOnWriteArrayList() }
        list.add(emitter)
        emitter.onCompletion { list.remove(emitter) }
        emitter.onTimeout { list.remove(emitter) }
        emitter.onError { list.remove(emitter) }
        return emitter
    }

    fun emitProgress(jobId: Long, payload: Any) = send(jobId, "progress", payload)

    fun emitDone(jobId: Long, payload: Any) {
        send(jobId, "done", payload)
        complete(jobId)
    }

    private fun send(jobId: Long, event: String, payload: Any) {
        val list = emitters[jobId] ?: return
        list.forEach { emitter ->
            runCatching {
                emitter.send(SseEmitter.event().name(event).data(payload))
            }.onFailure {
                log.debug("SSE send failed for {}: {}", jobId, it.message)
                list.remove(emitter)
            }
        }
    }

    private fun complete(jobId: Long) {
        emitters.remove(jobId)?.forEach { runCatching { it.complete() } }
    }
}

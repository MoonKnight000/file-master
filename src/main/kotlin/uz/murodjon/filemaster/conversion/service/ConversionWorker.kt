package uz.murodjon.filemaster.conversion.service

/** Runs a conversion job asynchronously, streaming progress over [JobEvents]. */
interface ConversionWorker {
    fun process(jobId: Long)
}

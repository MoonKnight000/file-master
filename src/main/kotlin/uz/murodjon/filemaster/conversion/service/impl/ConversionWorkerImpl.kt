package uz.murodjon.filemaster.conversion.service.impl

import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import uz.murodjon.filemaster.common.JobStatus
import uz.murodjon.filemaster.config.AppProperties
import uz.murodjon.filemaster.conversion.processor.ConversionProcessor
import uz.murodjon.filemaster.conversion.processor.ConversionSupport
import uz.murodjon.filemaster.conversion.repository.ConversionJobRepository
import uz.murodjon.filemaster.conversion.service.ConversionWorker
import uz.murodjon.filemaster.tools.enums.ToolEngine
import uz.murodjon.filemaster.tools.enums.ToolGroup
import uz.murodjon.filemaster.tools.service.ToolProvider
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.io.path.deleteRecursively

/**
 * Thin router: loads the job, picks the [ConversionProcessor] for the tool's file-type group,
 * and runs it inside a fresh scratch directory. All type-specific conversion lives in the
 * processors; all shared plumbing lives in [ConversionSupport].
 */
@Component
class ConversionWorkerImpl(
    private val jobs: ConversionJobRepository,
    private val tools: ToolProvider,
    private val support: ConversionSupport,
    private val props: AppProperties,
    processors: List<ConversionProcessor>,
) : ConversionWorker {

    private val log = LoggerFactory.getLogger(javaClass)
    private val byGroup: Map<ToolGroup, ConversionProcessor> = processors.associateBy { it.group }

    @Async("conversionExecutor")
    @OptIn(kotlin.io.path.ExperimentalPathApi::class)
    override fun process(jobId: Long) {
        val job = jobs.findById(jobId).orElse(null) ?: return
        val tool = tools.findBySlug(job.tool.slug) ?: return

        if (tool.engine == ToolEngine.NONE) {
            support.failJob(job, "This tool is not available yet.")
            return
        }
        val processor = byGroup[ToolGroup.of(tool.category)] ?: run {
            log.warn("No processor for group {} (job {})", ToolGroup.of(tool.category), jobId)
            support.failJob(job, "This tool is not available yet.")
            return
        }

        val workDir = Paths.get(props.tools.workDir).resolve(jobId.toString())
        Files.createDirectories(workDir)

        job.status = JobStatus.PROCESSING
        jobs.save(job)
        support.emitProgress(job)

        try {
            processor.process(job, tool, workDir)
        } finally {
            runCatching { workDir.deleteRecursively() }
        }
    }
}

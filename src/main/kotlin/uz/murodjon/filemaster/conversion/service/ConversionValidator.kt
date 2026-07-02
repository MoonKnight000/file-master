package uz.murodjon.filemaster.conversion.service

import org.springframework.stereotype.Component
import uz.murodjon.filemaster.auth.model.User
import uz.murodjon.filemaster.common.FileCategories
import uz.murodjon.filemaster.config.AppProperties
import uz.murodjon.filemaster.exception.PlanLimitException
import uz.murodjon.filemaster.exception.ToolNotFoundException
import uz.murodjon.filemaster.exception.UnsupportedFormatException
import uz.murodjon.filemaster.auth.enums.UserPlan
import uz.murodjon.filemaster.exception.UploadNotFoundException
import uz.murodjon.filemaster.exception.ValidationException
import uz.murodjon.filemaster.files.model.StoredFile
import uz.murodjon.filemaster.files.repository.StoredFileRepository
import uz.murodjon.filemaster.tools.dto.ToolDef
import uz.murodjon.filemaster.tools.enums.ToolEngine
import uz.murodjon.filemaster.tools.enums.ToolGroup
import uz.murodjon.filemaster.tools.enums.ToolSlug
import uz.murodjon.filemaster.tools.service.ToolProvider

/**
 * The file-type-agnostic conversion checks each category service composes (tool, inputs,
 * output format). Category-specific option validation lives in [MediaValidator]; this only
 * covers what's identical across audio/video/image/document/archive, so it isn't duplicated
 * five times.
 */
@Component
class ConversionValidator(
    private val tools: ToolProvider,
    private val files: StoredFileRepository,
    private val props: AppProperties,
) {

    /** Resolves the requested tool and asserts it belongs to [group] and has a working engine. */
    fun requireTool(slug: ToolSlug?, group: ToolGroup): ToolDef {
        val toolSlug = slug ?: throw ValidationException("`tool` is required.")
        val tool = tools.findBySlug(toolSlug.slug) ?: throw ToolNotFoundException(toolSlug.slug)
        if (ToolGroup.of(tool.category) != group) {
            throw ValidationException("'${toolSlug.slug}' is not a ${group.value} tool.")
        }
        if (tool.engine == ToolEngine.NONE) {
            throw UnsupportedFormatException("The '${toolSlug.slug}' tool is not available yet.")
        }
        return tool
    }

    /**
     * Resolves the user's active uploads for [fileIds], enforcing the batch-size limit and that
     * every input is an accepted format for [tool].
     */
    fun resolveInputs(user: User, fileIds: List<Long>?, tool: ToolDef): List<StoredFile> {
        val ids = fileIds?.takeIf { it.isNotEmpty() }
            ?: throw ValidationException("`fileIds` must not be empty.")
        if (ids.size > 1 && user.plan != UserPlan.PREMIUM && !props.limits.freeBatchConvert) {
            throw PlanLimitException("Batch conversion is a Pro feature.")
        }

        val resolved = ids.map { id ->
            files.findByIdAndUserIdAndActiveTrue(id, user.id!!).orElse(null)
                ?: throw UploadNotFoundException("Upload not found: $id")
        }
        // "*" in accept = any file type (e.g. zip-files packs whatever the user selects).
        if ("*" !in tool.accept) {
            resolved.forEach { source ->
                if (".${FileCategories.extOf(source.originalName)}" !in tool.accept) {
                    throw UnsupportedFormatException(
                        "${source.originalName} is not accepted by ${tool.title}.",
                        details = mapOf("accept" to tool.accept),
                    )
                }
            }
        }
        return resolved
    }

    /** Resolves the effective output format (defaulting from [tool]) and checks it's supported. */
    fun resolveOutputFormat(tool: ToolDef, requested: String?): String {
        val outputFormat = (requested ?: tool.defaultFormat.value).uppercase()
        if (tool.outputFormats.none { it.value.equals(outputFormat, ignoreCase = true) }) {
            throw ValidationException(
                "Unsupported output format: $outputFormat",
                details = mapOf("outputFormats" to tool.outputFormats.map { it.value }),
            )
        }
        return outputFormat
    }
}

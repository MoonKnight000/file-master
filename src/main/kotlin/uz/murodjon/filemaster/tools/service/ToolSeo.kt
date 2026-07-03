package uz.murodjon.filemaster.tools.service

import uz.murodjon.filemaster.tools.dto.ToolDef
import uz.murodjon.filemaster.tools.dto.ToolFaqEntry
import uz.murodjon.filemaster.tools.enums.ToolKind

/**
 * Derives the SEO landing-page copy (long description + FAQ) for a tool from its
 * catalog definition. Content is deterministic — same input, same text — so the
 * front-end can safely cache/prerender it. Edit the templates here to change every
 * tool page at once; per-tool overrides belong in the seeder if ever needed.
 */
object ToolSeo {

    fun longDescription(tool: ToolDef): String {
        val inputs = tool.accept.filter { it != "*" }.joinToString(", ")
        val outputs = tool.outputFormats.joinToString(", ") { it.value.uppercase() }
        val action = when (tool.kind) {
            ToolKind.EDIT -> "edit your files"
            ToolKind.COMPRESS -> "shrink your files without losing quality where possible"
            ToolKind.MERGE -> "combine multiple files into one"
            ToolKind.OCR -> "extract selectable text from scans and images"
            else -> "convert your files"
        }
        val acceptSentence =
            if (inputs.isBlank()) "It accepts any file type"
            else "It accepts $inputs"
        val outputSentence =
            if (outputs.isBlank()) "" else " and produces $outputs"

        return "${tool.title} lets you $action right in your browser — no software to install. " +
            "$acceptSentence$outputSentence. " +
            "Files are processed on our servers over an encrypted connection and removed " +
            "automatically after the retention window, so nothing stays behind."
    }

    fun faq(tool: ToolDef): List<ToolFaqEntry> {
        val outputs = tool.outputFormats.joinToString(", ") { it.value.uppercase() }
        return listOf(
            ToolFaqEntry(
                "How do I use ${tool.title}?",
                "Upload your file(s), pick the options you need and press Convert. " +
                    "You can download the result as soon as the progress bar completes.",
            ),
            ToolFaqEntry(
                "Is ${tool.title} free?",
                "Yes — every tool is free with a daily limit. Creating an account raises the " +
                    "limit, and Pro removes it entirely and keeps your results longer.",
            ),
            ToolFaqEntry(
                "Which formats are supported?",
                if (outputs.isBlank()) "The tool works with the file types shown on the upload box."
                else "Results can be saved as $outputs. The upload box shows every accepted input type.",
            ),
            ToolFaqEntry(
                "Are my files safe?",
                "Transfers are encrypted and files are deleted automatically after the retention " +
                    "period (longer on Pro). You can also delete anything yourself from My Files.",
            ),
        )
    }
}

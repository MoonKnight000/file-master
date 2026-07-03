package uz.murodjon.filemaster.audio.service.impl

import org.springframework.stereotype.Service
import uz.murodjon.filemaster.audio.service.AudioConversionService
import uz.murodjon.filemaster.audio.model.AudioConversionJob
import uz.murodjon.filemaster.auth.model.User
import uz.murodjon.filemaster.audio.dto.AudioConvertRequest
import uz.murodjon.filemaster.conversion.dto.ConversionOptions
import uz.murodjon.filemaster.conversion.dto.JobDto
import uz.murodjon.filemaster.conversion.service.ConversionService
import uz.murodjon.filemaster.conversion.service.ConversionValidator
import uz.murodjon.filemaster.conversion.service.MediaValidator
import uz.murodjon.filemaster.common.Quality
import uz.murodjon.filemaster.tools.enums.EditOperation
import uz.murodjon.filemaster.tools.enums.ToolGroup
import uz.murodjon.filemaster.tools.enums.ToolSlug

@Service
class AudioConversionServiceImpl(
    private val conversion: ConversionService,
    private val validator: ConversionValidator,
    private val mediaValidator: MediaValidator,
) : AudioConversionService {

    override fun convert(user: User, request: AudioConvertRequest): JobDto {
        val tool = validator.requireTool(request.tool, ToolGroup.AUDIO)
        val sources = validator.resolveInputs(user, request.fileIds, tool)

        val o = request.options
        mediaValidator.validateAudio(
            ConversionOptions(
                audioBitrateKbps = o?.audioBitrateKbps,
                audioSampleRate = o?.audioSampleRate,
                audioChannels = o?.audioChannels,
                audioVolume = o?.audioVolume,
                audioNormalize = o?.audioNormalize,
                trimStartSeconds = o?.trimStartSeconds,
                trimEndSeconds = o?.trimEndSeconds,
                speedFactor = o?.speedFactor,
                fadeInSeconds = o?.fadeInSeconds,
                fadeOutSeconds = o?.fadeOutSeconds,
            ),
            tool.editOperation,
        )

        val format = validator.resolveOutputFormat(tool, o?.outputFormat)
        val job = AudioConversionJob(
            user = user,
            tool = ToolSlug.from(tool.slug),
            outputFormat = format,
            quality = o?.quality ?: Quality.BALANCED,
            keepOriginal = o?.keepOriginal ?: tool.keepOriginalDefault,
            mergeIntoOne = tool.mergeIntoOneDefault,
            audioBitrateKbps = o?.audioBitrateKbps,
            audioSampleRate = o?.audioSampleRate,
            audioChannels = o?.audioChannels,
            audioVolume = o?.audioVolume,
            // normalize-audio / reverse-audio need no knobs — the tool itself implies the effect.
            audioNormalize = o?.audioNormalize ?: (tool.editOperation == EditOperation.NORMALIZE),
            trimStartSeconds = o?.trimStartSeconds,
            trimEndSeconds = o?.trimEndSeconds,
            speedFactor = o?.speedFactor,
            fadeInSeconds = o?.fadeInSeconds,
            fadeOutSeconds = o?.fadeOutSeconds,
            reverseAudio = tool.editOperation == EditOperation.REVERSE,
        )
        return conversion.submit(job, sources)
    }
}
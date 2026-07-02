package uz.murodjon.filemaster.video.service

import org.springframework.stereotype.Service
import uz.murodjon.filemaster.auth.model.User
import uz.murodjon.filemaster.conversion.dto.ConversionOptions
import uz.murodjon.filemaster.conversion.dto.JobDto
import uz.murodjon.filemaster.video.dto.VideoConvertRequest
import uz.murodjon.filemaster.conversion.service.ConversionService
import uz.murodjon.filemaster.conversion.service.ConversionValidator
import uz.murodjon.filemaster.conversion.service.MediaValidator
import uz.murodjon.filemaster.tools.enums.ToolGroup
import uz.murodjon.filemaster.common.Quality
import uz.murodjon.filemaster.tools.enums.ToolSlug
import uz.murodjon.filemaster.video.model.VideoConversionJob

@Service
class VideoConversionServiceImpl(
    private val conversion: ConversionService,
    private val validator: ConversionValidator,
    private val mediaValidator: MediaValidator,
) : VideoConversionService {

    override fun convert(user: User, request: VideoConvertRequest): JobDto {
        val tool = validator.requireTool(request.tool, ToolGroup.VIDEO)
        val sources = validator.resolveInputs(user, request.fileIds, tool)

        val o = request.options
        // Output format drives codec<->container compatibility, so resolve it before validating.
        val format = validator.resolveOutputFormat(tool, o?.outputFormat)
        val isMuted = o?.muteAudio == true
        val opts = ConversionOptions(
            videoResolution = o?.videoResolution,
            videoFps = o?.videoFps,
            videoBitrateKbps = o?.videoBitrateKbps,
            videoCodec = o?.videoCodec,
            muteAudio = o?.muteAudio,
            audioBitrateKbps = if (isMuted) null else o?.audioBitrateKbps,
            audioSampleRate = if (isMuted) null else o?.audioSampleRate,
            audioChannels = if (isMuted) null else o?.audioChannels,
            audioVolume = if (isMuted) null else o?.audioVolume,
            audioNormalize = if (isMuted) null else o?.audioNormalize,
            trimStartSeconds = o?.trimStartSeconds,
            trimEndSeconds = o?.trimEndSeconds,
        )
        mediaValidator.validateVideo(opts, format)
        if (!isMuted) mediaValidator.validateAudio(opts)

        val job = VideoConversionJob(
            user = user,
            tool = ToolSlug.from(tool.slug),
            outputFormat = format,
            quality = o?.quality ?: Quality.BALANCED,
            keepOriginal = o?.keepOriginal ?: tool.keepOriginalDefault,
            mergeIntoOne = tool.mergeIntoOneDefault,
            videoResolution = o?.videoResolution,
            videoFps = o?.videoFps,
            videoBitrateKbps = o?.videoBitrateKbps,
            videoCodec = o?.videoCodec,
            muteAudio = o?.muteAudio ?: false,
            audioBitrateKbps = if (isMuted) null else o?.audioBitrateKbps,
            audioSampleRate = if (isMuted) null else o?.audioSampleRate,
            audioChannels = if (isMuted) null else o?.audioChannels,
            audioVolume = if (isMuted) null else o?.audioVolume,
            audioNormalize = if (isMuted) false else (o?.audioNormalize ?: false),
            trimStartSeconds = o?.trimStartSeconds,
            trimEndSeconds = o?.trimEndSeconds,
        )
        return conversion.submit(job, sources)
    }
}
package uz.murodjon.filemaster.audio.model

import jakarta.persistence.Entity
import jakarta.persistence.Table
import uz.murodjon.filemaster.auth.model.User
import uz.murodjon.filemaster.common.Quality
import uz.murodjon.filemaster.conversion.engine.ConversionSettings
import uz.murodjon.filemaster.conversion.model.ConversionJob
import uz.murodjon.filemaster.tools.enums.ToolSlug

/**
 * Audio conversion job: carries the audio-only knobs (joined to [ConversionJob] on its id).
 * These columns live here and nowhere else — no other category reads them.
 */
@Entity
@Table(name = "audio_conversion_jobs")
class AudioConversionJob(
    user: User,
    tool: ToolSlug,
    outputFormat: String,
    quality: Quality = Quality.BALANCED,
    keepOriginal: Boolean = true,
    mergeIntoOne: Boolean = false,
    // null = engine default / derive from quality.
    var audioBitrateKbps: Int? = null,
    var audioSampleRate: Int? = null,
    var audioChannels: Int? = null,
    var audioVolume: Double? = null,
    var audioNormalize: Boolean = false,
    var trimStartSeconds: Double? = null,
    var trimEndSeconds: Double? = null,
    var speedFactor: Double? = null,
    var fadeInSeconds: Double? = null,
    var fadeOutSeconds: Double? = null,
    var reverseAudio: Boolean = false,
    var transcribeLanguage: String? = null,
) : ConversionJob(user, tool, outputFormat, quality, keepOriginal, mergeIntoOne) {

    override fun toSettings(): ConversionSettings = super.toSettings().copy(
        audioBitrateKbps = audioBitrateKbps,
        audioSampleRate = audioSampleRate,
        audioChannels = audioChannels,
        audioVolume = audioVolume,
        audioNormalize = audioNormalize,
        trimStartSeconds = trimStartSeconds,
        trimEndSeconds = trimEndSeconds,
        speedFactor = speedFactor,
        fadeInSeconds = fadeInSeconds,
        fadeOutSeconds = fadeOutSeconds,
        reverseAudio = reverseAudio,
        transcribeLanguage = transcribeLanguage,
    )
}

package uz.murodjon.filemaster.tools.dto

/** Allowed audio knobs the front-end should render for audio tools. */
data class AudioOptions(
    val bitratesKbps: List<Int>,
    val sampleRates: List<Int>,
    val channels: List<Int>,
)

package uz.murodjon.filemaster.tools.dto

/** Allowed video knobs the front-end should render for video tools. */
data class VideoOptions(
    val resolutions: List<String>,
    val fps: List<Int>,
    val codecs: List<String>,
    val maxBitrateKbps: Int,
    /** Codecs allowed per output format, so the UI can filter codecs by the chosen format. */
    val codecsByFormat: Map<String, List<String>>,
)

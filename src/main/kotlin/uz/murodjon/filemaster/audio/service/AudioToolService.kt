package uz.murodjon.filemaster.audio.service

import uz.murodjon.filemaster.tools.dto.ToolListResponse

/** Lists the audio tools (the `/v1/audio/tools` endpoint), read from the DB catalog. */
interface AudioToolService {
    fun list(): ToolListResponse
}

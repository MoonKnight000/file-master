package uz.murodjon.filemaster.tools.service

import uz.murodjon.filemaster.tools.dto.ToolListResponse

/** Lists the video tools (the `/v1/video/tools` endpoint), read from the DB catalog. */
interface VideoToolService {
    fun list(): ToolListResponse
}

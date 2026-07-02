package uz.murodjon.filemaster.tools.service

import uz.murodjon.filemaster.tools.dto.ToolListResponse

/** Lists the image tools (the `/v1/image/tools` endpoint), read from the DB catalog. */
interface ImageToolService {
    fun list(): ToolListResponse
}

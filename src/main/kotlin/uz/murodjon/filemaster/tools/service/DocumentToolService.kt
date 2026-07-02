package uz.murodjon.filemaster.tools.service

import uz.murodjon.filemaster.tools.dto.ToolListResponse

/** Lists the document tools (the `/v1/document/tools` endpoint), read from the DB catalog. */
interface DocumentToolService {
    fun list(): ToolListResponse
}

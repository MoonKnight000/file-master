package uz.murodjon.filemaster.tools.service

import uz.murodjon.filemaster.tools.dto.ToolListResponse

/** Lists the archive tools (the `/v1/archive/tools` endpoint), read from the DB catalog. */
interface ArchiveToolService {
    fun list(): ToolListResponse
}

package uz.murodjon.filemaster.tools.service.impl

import org.springframework.stereotype.Service
import uz.murodjon.filemaster.tools.dto.ToolListResponse
import uz.murodjon.filemaster.tools.enums.ToolGroup
import uz.murodjon.filemaster.tools.service.ArchiveToolService
import uz.murodjon.filemaster.tools.service.ToolProvider

@Service
class ArchiveToolServiceImpl(private val provider: ToolProvider) : ArchiveToolService {
    override fun list(): ToolListResponse =
        ToolListResponse(provider.byGroup(ToolGroup.ARCHIVE))
}

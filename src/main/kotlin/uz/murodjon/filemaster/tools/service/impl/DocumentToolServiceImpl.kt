package uz.murodjon.filemaster.tools.service.impl

import org.springframework.stereotype.Service
import uz.murodjon.filemaster.tools.dto.ToolListResponse
import uz.murodjon.filemaster.tools.enums.ToolGroup
import uz.murodjon.filemaster.tools.service.DocumentToolService
import uz.murodjon.filemaster.tools.service.ToolProvider

@Service
class DocumentToolServiceImpl(private val provider: ToolProvider) : DocumentToolService {
    override fun list(): ToolListResponse =
        ToolListResponse(provider.byGroup(ToolGroup.DOCUMENT))
}

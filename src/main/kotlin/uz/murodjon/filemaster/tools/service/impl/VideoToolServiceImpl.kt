package uz.murodjon.filemaster.tools.service.impl

import org.springframework.stereotype.Service
import uz.murodjon.filemaster.tools.dto.ToolListResponse
import uz.murodjon.filemaster.tools.enums.ToolGroup
import uz.murodjon.filemaster.tools.service.ToolProvider
import uz.murodjon.filemaster.tools.service.VideoToolService

@Service
class VideoToolServiceImpl(private val provider: ToolProvider) : VideoToolService {
    override fun list(): ToolListResponse =
        ToolListResponse(provider.byGroup(ToolGroup.VIDEO))
}

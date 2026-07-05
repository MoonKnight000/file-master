package uz.murodjon.filemaster.image.service.impl

import org.springframework.stereotype.Service
import uz.murodjon.filemaster.tools.dto.ToolListResponse
import uz.murodjon.filemaster.tools.enums.ToolGroup
import uz.murodjon.filemaster.image.service.ImageToolService
import uz.murodjon.filemaster.tools.service.ToolProvider

@Service
class ImageToolServiceImpl(private val provider: ToolProvider) : ImageToolService {
    override fun list(): ToolListResponse =
        ToolListResponse(provider.byGroup(ToolGroup.IMAGE))
}

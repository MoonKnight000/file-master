package uz.murodjon.filemaster.audio.service.impl

import org.springframework.stereotype.Service
import uz.murodjon.filemaster.tools.dto.ToolListResponse
import uz.murodjon.filemaster.tools.enums.ToolGroup
import uz.murodjon.filemaster.audio.service.AudioToolService
import uz.murodjon.filemaster.tools.service.ToolProvider

@Service
class AudioToolServiceImpl(private val provider: ToolProvider) : AudioToolService {
    override fun list(): ToolListResponse =
        ToolListResponse(provider.byGroup(ToolGroup.AUDIO))
}

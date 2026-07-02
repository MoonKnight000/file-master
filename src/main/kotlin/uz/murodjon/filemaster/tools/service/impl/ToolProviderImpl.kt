package uz.murodjon.filemaster.tools.service.impl

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uz.murodjon.filemaster.common.CategoryToken
import uz.murodjon.filemaster.tools.dto.ToolDef
import uz.murodjon.filemaster.tools.dto.toDef
import uz.murodjon.filemaster.tools.enums.ToolGroup
import uz.murodjon.filemaster.tools.repository.ToolRepository
import uz.murodjon.filemaster.tools.service.ToolProvider

@Service
@Transactional(readOnly = true) // open a tx so the lazy accept/outputFormats collections load while mapping
class ToolProviderImpl(private val tools: ToolRepository) : ToolProvider {

    override fun all(): List<ToolDef> =
        tools.findByActiveTrueOrderByIdAsc().map { it.toDef() }

    override fun byGroup(group: ToolGroup): List<ToolDef> =
        tools.findByCategoryInAndActiveTrueOrderByIdAsc(group.categories).map { it.toDef() }

    override fun byCategory(category: String?): List<ToolDef> {
        if (category.isNullOrBlank() || category == "all") return all()
        val token = runCatching { CategoryToken.from(category) }.getOrNull() ?: return emptyList()
        return tools.findByCategoryAndActiveTrueOrderByIdAsc(token).map { it.toDef() }
    }

    override fun findBySlug(slug: String): ToolDef? =
        tools.findBySlug(slug)?.toDef()
}

package uz.murodjon.filemaster.tools.repository

import org.springframework.data.jpa.repository.JpaRepository
import uz.murodjon.filemaster.common.CategoryToken
import uz.murodjon.filemaster.tools.model.Tool

interface ToolRepository : JpaRepository<Tool, Long> {

    fun findBySlug(slug: String): Tool?

    fun findByActiveTrueOrderByIdAsc(): List<Tool>

    fun findByCategoryAndActiveTrueOrderByIdAsc(category: CategoryToken): List<Tool>

    fun findByCategoryInAndActiveTrueOrderByIdAsc(categories: Collection<CategoryToken>): List<Tool>
}

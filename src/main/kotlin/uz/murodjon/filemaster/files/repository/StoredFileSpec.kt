package uz.murodjon.filemaster.files.repository

import org.springframework.data.jpa.domain.Specification
import uz.murodjon.filemaster.common.CategoryToken
import uz.murodjon.filemaster.files.dto.FilesFilterRequest
import uz.murodjon.filemaster.files.enums.FileSource
import uz.murodjon.filemaster.files.model.StoredFile

object StoredFileSpec {

    fun build(userId: Long, filter: FilesFilterRequest): Specification<StoredFile> {
        var spec = Specification
            .where(belongsToUser(userId))
            .and(isActive())

        if (filter.category.size < CategoryToken.entries.size) {
            spec = spec.and(inCategories(filter.category))
        }

        if (filter.starred) {
            spec = spec.and(isStarred())
        }

        filter.query?.takeIf { it.isNotBlank() }?.let {
            spec = spec.and(nameContains(it))
        }

        filter.source?.let {
            spec = spec.and(hasSource(it))
        }

        return spec
    }

    private fun belongsToUser(userId: Long): Specification<StoredFile> =
        Specification { root, _, cb ->
            cb.equal(root.get<Any>("user").get<Long>("id"), userId)
        }

    private fun isActive(): Specification<StoredFile> =
        Specification { root, _, cb ->
            cb.isTrue(root.get("active"))
        }

    private fun inCategories(categories: List<CategoryToken>): Specification<StoredFile> =
        Specification { root, _, _ ->
            root.get<CategoryToken>("category").`in`(categories)
        }

    private fun isStarred(): Specification<StoredFile> =
        Specification { root, _, cb ->
            cb.isTrue(root.get("starred"))
        }

    private fun nameContains(query: String): Specification<StoredFile> =
        Specification { root, _, cb ->
            // Escape LIKE wildcards so "%"/"_" in a search term match literally.
            val escaped = query.lowercase()
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_")
            cb.like(cb.lower(root.get("originalName")), "%$escaped%", '\\')
        }

    private fun hasSource(source: FileSource): Specification<StoredFile> =
        Specification { root, _, cb ->
            cb.equal(root.get<FileSource>("source"), source)
        }
}

package uz.murodjon.filemaster.files.repository

import org.springframework.data.jpa.domain.Specification
import uz.murodjon.filemaster.common.CategoryToken
import uz.murodjon.filemaster.files.dto.FilesFilterRequest
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
}

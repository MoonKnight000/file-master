package uz.murodjon.filemaster.files.dto

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import org.springframework.data.domain.Sort
import uz.murodjon.filemaster.common.CategoryToken
import uz.murodjon.filemaster.files.enums.FileSource
import uz.murodjon.filemaster.files.enums.FilesFilterOrder
import uz.murodjon.filemaster.pageable.FilterInterface

data class FilesFilterRequest(
    @field:Min(1, message = "Page must be greater than or equal to 1")
    override val page: Int = 1,
    @field:Min(1, message = "Size must be greater than or equal to 1")
    @field:Max(100, message = "Size must be less than or equal to 100")
    override val size: Int = 10,
    override val orders: LinkedHashMap<FilesFilterOrder, Sort.Direction> = linkedMapOf(FilesFilterOrder.CREATED_TIMESTAMP to Sort.Direction.DESC),
    val category: List<CategoryToken> = CategoryToken.entries,
    val starred: Boolean = false,
    /** Case-insensitive substring match on the file name. */
    val query: String? = null,
    /** Restrict to uploads or conversion results; null = both. */
    val source: FileSource? = null,
) : FilterInterface<FilesFilterOrder>
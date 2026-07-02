package uz.murodjon.filemaster.util

import org.springframework.data.domain.Page

class PageableData<T : Any>(
    val totalPages: Int,
    val currentPage: Int,
    val totalElements: Long,
    var data: List<T>
) {
    constructor(page: Page<T>) : this(
        totalPages = page.totalPages,
        currentPage = page.number + 1,
        totalElements = page.totalElements,
        data = page.content,
    )

    constructor(page: Page<T>, data: List<T>) : this(
        totalPages = page.totalPages,
        currentPage = page.number + 1,
        totalElements = page.totalElements,
        data = data,
    )

}

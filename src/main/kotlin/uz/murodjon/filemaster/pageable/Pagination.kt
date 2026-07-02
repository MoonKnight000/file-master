package uz.murodjon.filemaster.pageable

import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import kotlin.collections.component1
import kotlin.collections.component2

/**
 * @author Murodjon on 27.06.2026
 */


/**
 * @param repository Entity uchun yaratilgan repository
 */
fun <T : Any, K : TableField, F : FilterInterface<K>> getPagination(
    filter: F,
    query: (Pageable) -> Page<T>,
): Page<T> {
    var page = filter.page
    val sort = if (filter.orders.isEmpty()) Sort.unsorted()
               else Sort.by(filter.orders.map { (field, dir) -> Sort.Order(dir, field.value) })

    var result = query(PageRequest.of(page - 1, filter.size, sort))
    if (result.totalPages in 1 until page) {
        page = result.totalPages
        result = query(PageRequest.of(page - 1, filter.size, sort))
    }
    return result
}

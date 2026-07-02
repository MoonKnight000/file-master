package uz.murodjon.filemaster.pageable

import org.springframework.data.domain.Sort

interface FilterInterface<T : TableField> {
    val page: Int
    val size: Int
    val orders: LinkedHashMap<T, Sort.Direction>
}
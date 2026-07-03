package uz.murodjon.filemaster.search.dto

import uz.murodjon.filemaster.common.CategoryToken

data class SearchToolDto(
    val slug: String,
    val title: String,
    val category: CategoryToken,
    val icon: String,
)

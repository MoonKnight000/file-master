package uz.murodjon.filemaster.search.dto

import uz.murodjon.filemaster.common.CategoryToken

data class SearchFileDto(
    val id: Long,
    val name: String,
    val category: CategoryToken,
    val downloadUrl: String,
)

package uz.murodjon.filemaster.auth.dto

import uz.murodjon.filemaster.common.CategoryToken

/** Per-category file count for the account usage breakdown. */
data class CategoryUsage(val category: CategoryToken, val fileCount: Long)

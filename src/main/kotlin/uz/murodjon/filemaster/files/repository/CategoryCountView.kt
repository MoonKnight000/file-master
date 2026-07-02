package uz.murodjon.filemaster.files.repository

import uz.murodjon.filemaster.common.CategoryToken

/** Spring Data projection for a per-category file count (account usage breakdown). */
interface CategoryCountView {
    val category: CategoryToken
    val count: Long
}

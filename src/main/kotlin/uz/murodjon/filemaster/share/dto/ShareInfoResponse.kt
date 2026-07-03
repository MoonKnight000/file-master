package uz.murodjon.filemaster.share.dto

import uz.murodjon.filemaster.common.CategoryToken

/**
 * Public preview of a shared file — lets the front-end render a share landing page
 * (name, size, type) before the visitor clicks Download.
 */
data class ShareInfoResponse(
    val name: String,
    val bytes: Long,
    val format: String,
    val category: CategoryToken,
    val expiresTimestamp: Long,
    val downloadCount: Long,
    val downloadUrl: String,
)

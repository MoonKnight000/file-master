package uz.murodjon.filemaster.share.dto

data class ShareLinkResponse(
    val token: String,
    val expiresTimestamp: Long,
    val publicUrl: String,
)

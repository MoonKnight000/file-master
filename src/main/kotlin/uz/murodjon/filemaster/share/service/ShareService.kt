package uz.murodjon.filemaster.share.service

import uz.murodjon.filemaster.auth.model.User
import uz.murodjon.filemaster.files.dto.FileDownload
import uz.murodjon.filemaster.share.dto.CreateShareRequest
import uz.murodjon.filemaster.share.dto.ShareInfoResponse
import uz.murodjon.filemaster.share.dto.ShareLinkResponse

interface ShareService {
    fun create(user: User, fileId: Long, request: CreateShareRequest): ShareLinkResponse

    /** Public metadata for the share landing page (no auth). */
    fun info(token: String): ShareInfoResponse

    /** Public download; counts each successful resolve. */
    fun resolve(token: String): FileDownload

    /** Deactivates one of the caller's own share links. */
    fun revoke(user: User, token: String)
}

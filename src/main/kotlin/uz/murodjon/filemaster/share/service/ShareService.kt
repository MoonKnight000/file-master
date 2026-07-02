package uz.murodjon.filemaster.share.service

import uz.murodjon.filemaster.auth.model.User
import uz.murodjon.filemaster.files.dto.FileDownload
import uz.murodjon.filemaster.share.dto.CreateShareRequest
import uz.murodjon.filemaster.share.dto.ShareLinkResponse

interface ShareService {
    fun create(user: User, fileId: Long, request: CreateShareRequest): ShareLinkResponse
    fun resolve(token: String): FileDownload
}

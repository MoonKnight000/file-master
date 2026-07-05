package uz.murodjon.filemaster.share.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uz.murodjon.filemaster.auth.model.User
import uz.murodjon.filemaster.common.Ids
import uz.murodjon.filemaster.exception.NotFoundException
import uz.murodjon.filemaster.exception.ResultFileNotFoundException
import uz.murodjon.filemaster.exception.ValidationException
import uz.murodjon.filemaster.files.dto.FileDownload
import uz.murodjon.filemaster.files.repository.StoredFileRepository
import uz.murodjon.filemaster.share.dto.CreateShareRequest
import uz.murodjon.filemaster.share.dto.ShareInfoResponse
import uz.murodjon.filemaster.share.dto.ShareLinkResponse
import uz.murodjon.filemaster.share.model.ShareLink
import uz.murodjon.filemaster.share.repository.ShareLinkRepository
import uz.murodjon.filemaster.share.service.ShareService
import uz.murodjon.filemaster.storage.StorageService
import java.time.Instant

@Service
class ShareServiceImpl(
    private val shareLinks: ShareLinkRepository,
    private val files: StoredFileRepository,
    private val storage: StorageService,
) : ShareService {

    @Transactional
    override fun create(user: User, fileId: Long, request: CreateShareRequest): ShareLinkResponse {
        val ttl = request.ttlHours  // 1..168 enforced by Bean Validation on CreateShareRequest (@Valid)
        val file = files.findByIdAndUserIdAndActiveTrue(fileId, user.id!!).orElse(null)
            ?: throw ResultFileNotFoundException()

        val expiresTimestamp = Instant.now().epochSecond + ttl * 3600L
        val token = Ids.token()
        shareLinks.save(ShareLink(token = token, file = file, user = user, expiresTimestamp = expiresTimestamp))

        return ShareLinkResponse(
            token = token,
            expiresTimestamp = expiresTimestamp,
            publicUrl = "/v1/share/$token",
        )
    }

    @Transactional(readOnly = true)
    override fun info(token: String): ShareInfoResponse {
        val link = loadLive(token)
        val file = link.file
        return ShareInfoResponse(
            name = file.originalName,
            bytes = file.bytes,
            format = file.format,
            category = file.category,
            expiresTimestamp = link.expiresTimestamp,
            downloadCount = link.downloadCount,
            downloadUrl = "/v1/share/$token",
        )
    }

    @Transactional
    override fun resolve(token: String): FileDownload {
        val link = loadLive(token)
        val file = link.file

        link.downloadCount += 1
        shareLinks.save(link)

        return FileDownload(
            name = file.originalName,
            contentType = file.contentType ?: "application/octet-stream",
            bytes = file.bytes,
            writeTo = { out -> storage.get(file.absolutePath).use { it.copyTo(out) } },
        )
    }

    @Transactional
    override fun revoke(user: User, token: String) {
        val link = shareLinks.findByTokenAndActiveTrue(token).orElse(null)
            ?: throw NotFoundException("Share link not found.")
        if (link.user.id != user.id) throw NotFoundException("Share link not found.")
        link.active = false
        shareLinks.save(link)
    }

    /** Loads an active, unexpired link whose file still exists. */
    private fun loadLive(token: String): ShareLink {
        val link = shareLinks.findByTokenAndActiveTrue(token).orElse(null)
            ?: throw NotFoundException("Share link not found or has expired.")
        if (link.expiresTimestamp < Instant.now().epochSecond) {
            throw ValidationException("Share link has expired.")
        }
        if (!link.file.active) throw NotFoundException("The shared file is no longer available.")
        return link
    }
}

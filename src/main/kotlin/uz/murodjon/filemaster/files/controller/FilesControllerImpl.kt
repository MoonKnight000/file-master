package uz.murodjon.filemaster.files.controller

import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody
import uz.murodjon.filemaster.auth.model.User
import uz.murodjon.filemaster.files.dto.FileDto
import uz.murodjon.filemaster.files.dto.FilesFilterRequest
import uz.murodjon.filemaster.files.dto.PatchFileRequest
import uz.murodjon.filemaster.files.service.FilesService
import uz.murodjon.filemaster.share.dto.CreateShareRequest
import uz.murodjon.filemaster.share.dto.ShareLinkResponse
import uz.murodjon.filemaster.share.service.ShareService
import uz.murodjon.filemaster.util.PageableData
import uz.murodjon.filemaster.util.ResponseData

@RestController
class FilesControllerImpl(
    private val filesService: FilesService,
    private val shareService: ShareService,
) : FilesController {

    override fun upload(
        files: List<MultipartFile>,
        user: User,
    ): ResponseEntity<ResponseData<List<FileDto>>> =
        ResponseEntity.status(HttpStatus.CREATED).body(ResponseData(filesService.upload(user, files)))

    override fun filter(
        filter: FilesFilterRequest,
        user: User,
    ): ResponseEntity<ResponseData<PageableData<FileDto>>> =
        ResponseEntity.ok(ResponseData(filesService.filter(user, filter)))

    override fun download(
        fileId: Long,
        user: User,
    ): ResponseEntity<StreamingResponseBody> {
        val file = filesService.download(user, fileId)
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"${file.name}\"")
            .contentType(MediaType.parseMediaType(file.contentType))
            .contentLength(file.bytes)
            .body(StreamingResponseBody { out -> file.writeTo(out) })
    }

    override fun patch(
        fileId: Long,
        body: PatchFileRequest,
        user: User,
    ): ResponseEntity<ResponseData<FileDto>> =
        ResponseEntity.ok(ResponseData(filesService.patch(user, fileId, body)))

    override fun delete(
        fileId: Long,
        user: User,
    ): ResponseEntity<ResponseData<Any>> {
        filesService.delete(user, fileId)
        return ResponseEntity.ok(ResponseData(null, "Deleted"))
    }

    override fun share(
        fileId: Long,
        body: CreateShareRequest?,
        user: User,
    ): ResponseEntity<ResponseData<ShareLinkResponse>> =
        ResponseEntity.ok(ResponseData(shareService.create(user, fileId, body ?: CreateShareRequest())))
}
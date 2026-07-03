package uz.murodjon.filemaster.files.controller

import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody
import uz.murodjon.filemaster.auth.model.User
import uz.murodjon.filemaster.auth.security.CurrentUser
import uz.murodjon.filemaster.files.dto.*
import uz.murodjon.filemaster.share.dto.CreateShareRequest
import uz.murodjon.filemaster.share.dto.ShareLinkResponse
import uz.murodjon.filemaster.util.PageableData
import uz.murodjon.filemaster.util.ResponseData

@RequestMapping("/v1/files")
interface FilesController {

    @PostMapping
    fun upload(
        @RequestParam("files") files: List<MultipartFile>,
        @CurrentUser user: User,
    ): ResponseEntity<ResponseData<List<FileDto>>>

    @PostMapping("/filter")
    fun filter(
        @RequestBody @Valid filter: FilesFilterRequest,
        @CurrentUser user: User,
    ): ResponseEntity<ResponseData<PageableData<FileDto>>>

    @GetMapping("/{fileId}/download")
    fun download(
        @PathVariable fileId: Long,
        @CurrentUser user: User,
    ): ResponseEntity<StreamingResponseBody>

    @PatchMapping("/{fileId}")
    fun patch(
        @PathVariable fileId: Long,
        @RequestBody body: PatchFileRequest,
        @CurrentUser user: User,
    ): ResponseEntity<ResponseData<FileDto>>

    @DeleteMapping("/{fileId}")
    fun delete(
        @PathVariable fileId: Long,
        @CurrentUser user: User,
    ): ResponseEntity<ResponseData<Any>>

    @PostMapping("/{fileId}/share")
    fun share(
        @PathVariable fileId: Long,
        @RequestBody(required = false) @Valid body: CreateShareRequest?,
        @CurrentUser user: User,
    ): ResponseEntity<ResponseData<ShareLinkResponse>>
}

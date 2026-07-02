package uz.murodjon.filemaster.files.service

import org.springframework.web.multipart.MultipartFile
import uz.murodjon.filemaster.auth.model.User
import uz.murodjon.filemaster.files.dto.FileDownload
import uz.murodjon.filemaster.files.dto.FileDto
import uz.murodjon.filemaster.files.dto.FilesFilterRequest
import uz.murodjon.filemaster.files.dto.PatchFileRequest
import uz.murodjon.filemaster.util.PageableData

interface FilesService {
    fun upload(user: User, files: List<MultipartFile>): List<FileDto>
    fun filter(user: User, filter: FilesFilterRequest): PageableData<FileDto>
    fun download(user: User, fileId: Long): FileDownload
    fun patch(user: User, fileId: Long, body: PatchFileRequest): FileDto
    fun delete(user: User, fileId: Long)
}

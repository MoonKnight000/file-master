package uz.murodjon.filemaster.files.service.impl

import org.springframework.stereotype.Service
import uz.murodjon.filemaster.common.CategoryToken
import uz.murodjon.filemaster.files.service.DimensionProbe
import uz.murodjon.filemaster.files.service.FilesService
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import uz.murodjon.filemaster.auth.model.User
import uz.murodjon.filemaster.common.FileCategories
import uz.murodjon.filemaster.common.Ids
import uz.murodjon.filemaster.config.AppProperties
import uz.murodjon.filemaster.exception.FileTooLargeException
import uz.murodjon.filemaster.exception.ResultFileNotFoundException
import uz.murodjon.filemaster.files.dto.FileDownload
import uz.murodjon.filemaster.files.dto.FileDto
import uz.murodjon.filemaster.files.dto.FilesFilterRequest
import uz.murodjon.filemaster.files.dto.PatchFileRequest
import uz.murodjon.filemaster.files.enums.FileSource
import uz.murodjon.filemaster.files.model.StoredFile
import uz.murodjon.filemaster.files.repository.StoredFileRepository
import uz.murodjon.filemaster.files.repository.StoredFileSpec
import uz.murodjon.filemaster.pageable.getPagination
import uz.murodjon.filemaster.storage.StorageService
import uz.murodjon.filemaster.util.PageableData
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.time.Instant
import java.util.UUID

@Service
class FilesServiceImpl(
    private val filesRepository: StoredFileRepository,
    private val storage: StorageService,
    private val props: AppProperties,
    private val dimensionProbe: DimensionProbe,
) : FilesService {

    @Transactional
    override fun upload(user: User, files: List<MultipartFile>): List<FileDto> {

        val entities = files.map { file ->
            if (file.size > props.limits.maxFileBytes) throw FileTooLargeException(props.limits.maxFileBytes)
            val originalName = file.originalFilename?.takeIf { it.isNotBlank() } ?: "file"
            val ext = FileCategories.extOf(originalName)
            val category = FileCategories.categoryOf(originalName)
            val uuid = UUID.randomUUID()
            StoredFile(
                user = user,
                originalName = originalName,
                name = Ids.objectName(uuid.toString(), ext),
                bytes = file.size,
                category = category,
                source = FileSource.UPLOAD,
                folder = category.folder,
                absolutePath = Ids.absolutePath(category.folder, uuid.toString(), ext),
                uuid = uuid,
                format = ext,
                contentType = file.contentType,
            )
        }
        filesRepository.saveAll(entities)
        files.zip(entities).forEach { (multipart, entity) ->
            // Images/videos take a temp-file detour so their pixel dimensions can be probed
            // (needed for submit-time crop validation); everything else streams straight through.
            if (entity.category == CategoryToken.IMAGE || entity.category == CategoryToken.VIDEO) {
                val tmp = Files.createTempFile("upload-", ".${entity.format}")
                try {
                    multipart.inputStream.use { Files.copy(it, tmp, StandardCopyOption.REPLACE_EXISTING) }
                    dimensionProbe.probe(tmp, entity.category)?.let { (w, h) ->
                        entity.width = w
                        entity.height = h
                    }
                    storage.putFile(entity.absolutePath, tmp, multipart.contentType)
                } finally {
                    Files.deleteIfExists(tmp)
                }
            } else {
                multipart.inputStream.use { storage.putStream(entity.absolutePath, it, multipart.size, multipart.contentType) }
            }
        }
        return entities.map { FileDto(it, props.limits.retentionMinutesFor(user.plan)) }
    }

    @Transactional(readOnly = true)
    override fun filter(user: User, filter: FilesFilterRequest): PageableData<FileDto> {
        val userId = user.id!!
        val result = getPagination(filter) { pageable ->
            filesRepository.findAll(StoredFileSpec.build(userId, filter), pageable)
        }
        return PageableData(
            totalPages = result.totalPages,
            currentPage = result.number + 1,
            totalElements = result.totalElements,
            data = result.content.map { FileDto(it, props.limits.retentionMinutesFor(user.plan)) },
        )
    }

    @Transactional(readOnly = true)
    override fun download(user: User, fileId: Long): FileDownload {
        val file = filesRepository.findByIdAndUserIdAndActiveTrue(fileId, user.id!!)
            .orElseThrow { ResultFileNotFoundException() }
        return FileDownload(
            name = file.originalName,
            contentType = file.contentType ?: "application/octet-stream",
            bytes = file.bytes,
            writeTo = { out -> storage.get(file.absolutePath).use { it.copyTo(out) } },
        )
    }

    @Transactional
    override fun patch(user: User, fileId: Long, body: PatchFileRequest): FileDto {
        val file = filesRepository.findByIdAndUserIdAndActiveTrue(fileId, user.id!!)
            .orElseThrow { ResultFileNotFoundException() }

        // Rename is pure file metadata -> stays on StoredFile.
        body.name?.takeIf { it.isNotBlank() }?.let { file.originalName = it }
        body.starred?.let { file.starred = it }
        file.updatedTimestamp = Instant.now().epochSecond
        filesRepository.save(file)
        return FileDto(file, props.limits.retentionMinutesFor(user.plan))
    }

    @Transactional
    override fun delete(user: User, fileId: Long) {
        val file = filesRepository.findByIdAndUserIdAndActiveTrue(fileId, user.id!!).orElse(null)
            ?: throw ResultFileNotFoundException()
        storage.delete(file.absolutePath)
        file.active = false
        file.deletedTimestamp = Instant.now().epochSecond
        filesRepository.save(file)
    }
}
package uz.murodjon.filemaster.storage

import io.minio.*
import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uz.murodjon.filemaster.storage.StorageService
import uz.murodjon.filemaster.config.AppProperties
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Path

@Service
class MinioStorageService(
    private val minio: MinioClient,
    private val props: AppProperties,
) : StorageService {

    private val log = LoggerFactory.getLogger(javaClass)
    private val bucket get() = props.storage.bucket

    @PostConstruct
    fun ensureBucket() {
        val exists = minio.bucketExists(BucketExistsArgs.builder().bucket(bucket).build())
        if (!exists) {
            minio.makeBucket(MakeBucketArgs.builder().bucket(bucket).build())
            log.info("Created MinIO bucket '{}'", bucket)
        }
    }

    override fun putStream(key: String, stream: InputStream, size: Long, contentType: String?) {
        minio.putObject(
            PutObjectArgs.builder()
                .bucket(bucket)
                .`object`(key)
                .stream(stream, size, -1)
                .contentType(contentType ?: "application/octet-stream")
                .build(),
        )
    }

    override fun putFile(key: String, file: Path, contentType: String?) {
        Files.newInputStream(file).use { putStream(key, it, Files.size(file), contentType) }
    }

    override fun get(key: String): InputStream =
        minio.getObject(GetObjectArgs.builder().bucket(bucket).`object`(key).build())

    override fun download(key: String, dest: Path) {
        get(key).use { input -> Files.copy(input, dest) }
    }

    override fun delete(key: String) {
        runCatching {
            minio.removeObject(RemoveObjectArgs.builder().bucket(bucket).`object`(key).build())
        }.onFailure { log.warn("Failed to delete object {}: {}", key, it.message) }
    }
}

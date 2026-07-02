package uz.murodjon.filemaster.config

import io.minio.MinioClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class MinioConfig(private val props: AppProperties) {

    @Bean
    fun minioClient(): MinioClient =
        MinioClient.builder()
            .endpoint(props.storage.endpoint)
            .credentials(props.storage.accessKey, props.storage.secretKey)
            .build()
}

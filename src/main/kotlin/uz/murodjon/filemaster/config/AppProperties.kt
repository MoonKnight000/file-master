package uz.murodjon.filemaster.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app")
data class AppProperties(
    val storage: StorageProperties,
    val limits: LimitsProperties,
    val tools: ToolsProperties,
    val cors: CorsProperties,
    val google: GoogleProperties = GoogleProperties(),
    val billing: BillingProperties = BillingProperties(),
)

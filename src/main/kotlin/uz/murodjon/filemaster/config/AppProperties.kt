package uz.murodjon.filemaster.config

import org.springframework.boot.context.properties.ConfigurationProperties
import uz.murodjon.filemaster.auth.config.AuthProperties
import uz.murodjon.filemaster.auth.config.GoogleProperties
import uz.murodjon.filemaster.billing.config.BillingProperties
import uz.murodjon.filemaster.conversion.config.ToolsProperties
import uz.murodjon.filemaster.security.config.CorsProperties
import uz.murodjon.filemaster.storage.config.StorageProperties

/**
 * Root `app.*` binding. Feature-owned children live in their feature module's `config/`
 * subpackage (billing/auth/storage/security/conversion); only this root and the
 * cross-module [LimitsProperties] stay in the top-level `config`.
 */
@ConfigurationProperties(prefix = "app")
data class AppProperties(
    val storage: StorageProperties,
    val limits: LimitsProperties,
    val tools: ToolsProperties,
    val cors: CorsProperties,
    val google: GoogleProperties = GoogleProperties(),
    val billing: BillingProperties = BillingProperties(),
    val auth: AuthProperties = AuthProperties(),
)

package uz.murodjon.filemaster.billing.dto

import jakarta.validation.constraints.Positive
import uz.murodjon.filemaster.auth.enums.UserPlan

/**
 * Provider-agnostic payment confirmation. The bridge that adapts a real provider's callback
 * (Payme/Click) posts this shape with the shared `X-Webhook-Secret` header.
 */
data class BillingWebhookRequest(
    val userId: Long,
    val plan: UserPlan,
    /** Paid period in days; the plan lapses back to FREE after it. */
    @field:Positive(message = "`days` must be positive")
    val days: Int = 30,
)
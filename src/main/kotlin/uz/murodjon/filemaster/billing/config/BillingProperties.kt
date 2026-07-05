package uz.murodjon.filemaster.billing.config

data class BillingProperties(
    /**
     * Shared secret the payment provider's server-to-server callback must present in the
     * `X-Webhook-Secret` header. Empty (default) disables the webhook endpoint entirely.
     */
    val webhookSecret: String = "",
)

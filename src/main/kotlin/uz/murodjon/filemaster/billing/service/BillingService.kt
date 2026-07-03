package uz.murodjon.filemaster.billing.service

import uz.murodjon.filemaster.auth.model.User
import uz.murodjon.filemaster.billing.dto.BillingWebhookRequest
import uz.murodjon.filemaster.billing.dto.CheckoutRequest
import uz.murodjon.filemaster.billing.dto.CheckoutResponse

interface BillingService {

    /** Starts a provider-hosted checkout and returns the redirect URL. */
    fun checkout(user: User, request: CheckoutRequest): CheckoutResponse

    /** Applies a confirmed payment: activates the plan for the paid period. */
    fun handleWebhook(secret: String?, request: BillingWebhookRequest)
}

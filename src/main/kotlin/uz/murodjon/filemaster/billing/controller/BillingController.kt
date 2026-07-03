package uz.murodjon.filemaster.billing.controller

import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import uz.murodjon.filemaster.auth.model.User
import uz.murodjon.filemaster.auth.security.CurrentUser
import uz.murodjon.filemaster.billing.dto.BillingWebhookRequest
import uz.murodjon.filemaster.billing.dto.CheckoutRequest
import uz.murodjon.filemaster.billing.dto.CheckoutResponse
import uz.murodjon.filemaster.util.ResponseData

@RequestMapping("/v1/billing")
interface BillingController {

    @PostMapping("/checkout")
    fun checkout(
        @RequestBody request: CheckoutRequest,
        @CurrentUser user: User,
    ): ResponseEntity<ResponseData<CheckoutResponse>>

    /**
     * Server-to-server payment confirmation (public path, guarded by the shared
     * `X-Webhook-Secret` header — see `app.billing.webhook-secret`).
     */
    @PostMapping("/webhook")
    fun webhook(
        @RequestHeader("X-Webhook-Secret", required = false) secret: String?,
        @RequestBody @Valid request: BillingWebhookRequest,
    ): ResponseEntity<ResponseData<Any>>
}

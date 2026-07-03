package uz.murodjon.filemaster.billing.controller

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController
import uz.murodjon.filemaster.auth.model.User
import uz.murodjon.filemaster.billing.dto.BillingWebhookRequest
import uz.murodjon.filemaster.billing.dto.CheckoutRequest
import uz.murodjon.filemaster.billing.dto.CheckoutResponse
import uz.murodjon.filemaster.billing.service.BillingService
import uz.murodjon.filemaster.util.ResponseData

@RestController
class BillingControllerImpl(
    private val billingService: BillingService,
) : BillingController {

    override fun checkout(
        request: CheckoutRequest,
        user: User,
    ): ResponseEntity<ResponseData<CheckoutResponse>> =
        ResponseEntity.ok(ResponseData(billingService.checkout(user, request)))

    override fun webhook(
        secret: String?,
        request: BillingWebhookRequest,
    ): ResponseEntity<ResponseData<Any>> {
        billingService.handleWebhook(secret, request)
        return ResponseEntity.ok(ResponseData(message = "Plan activated"))
    }
}

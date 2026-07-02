package uz.murodjon.filemaster.billing.service

import org.springframework.stereotype.Service
import uz.murodjon.filemaster.auth.model.User
import uz.murodjon.filemaster.billing.dto.CheckoutRequest
import uz.murodjon.filemaster.billing.dto.CheckoutResponse

@Service
class BillingServiceImpl : BillingService {

    /**
     * Stub checkout. Wire a real provider (Stripe/Paddle) here and return its
     * hosted checkout URL. For now we echo back a placeholder so the front-end
     * "Upgrade" flow can be exercised end-to-end.
     */
    override fun checkout(user: User, request: CheckoutRequest): CheckoutResponse {
        val plan = request.plan
        val returnUrl = request.returnUrl ?: "http://localhost:3000/files"
        return CheckoutResponse(checkoutUrl = "$returnUrl?upgrade=$plan&status=stub")
    }
}
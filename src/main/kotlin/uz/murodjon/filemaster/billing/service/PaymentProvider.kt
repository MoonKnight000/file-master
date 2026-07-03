package uz.murodjon.filemaster.billing.service

import uz.murodjon.filemaster.auth.enums.UserPlan
import uz.murodjon.filemaster.auth.model.User

/**
 * Abstraction over the hosted-checkout payment provider (Payme/Click/Stripe/...).
 * Exactly one implementation is active; the stub is used until real credentials exist.
 */
interface PaymentProvider {

    /** Provider id used in logs (e.g. "stub", "payme", "click"). */
    val name: String

    /**
     * Creates a provider-hosted checkout for upgrading [user] to [plan] and returns the URL
     * the front-end should redirect to. The provider later confirms payment via the
     * `POST /v1/billing/webhook` callback.
     */
    fun createCheckoutUrl(user: User, plan: UserPlan, returnUrl: String): String
}

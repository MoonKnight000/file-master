package uz.murodjon.filemaster.billing.service.impl

import org.springframework.stereotype.Component
import uz.murodjon.filemaster.auth.enums.UserPlan
import uz.murodjon.filemaster.auth.model.User
import uz.murodjon.filemaster.billing.service.PaymentProvider

/**
 * Placeholder provider so the front-end "Upgrade" flow works end-to-end before a real
 * provider (Payme/Click) is wired. It never charges anyone — activation only happens
 * through the secret-guarded webhook.
 */
@Component
class StubPaymentProvider : PaymentProvider {

    override val name = "stub"

    override fun createCheckoutUrl(user: User, plan: UserPlan, returnUrl: String): String =
        "$returnUrl?upgrade=$plan&status=stub"
}

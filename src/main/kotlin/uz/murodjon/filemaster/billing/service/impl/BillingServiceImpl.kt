package uz.murodjon.filemaster.billing.service.impl

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uz.murodjon.filemaster.auth.model.User
import uz.murodjon.filemaster.auth.repository.UserRepository
import uz.murodjon.filemaster.billing.dto.BillingWebhookRequest
import uz.murodjon.filemaster.billing.dto.CheckoutRequest
import uz.murodjon.filemaster.billing.dto.CheckoutResponse
import uz.murodjon.filemaster.billing.service.BillingService
import uz.murodjon.filemaster.billing.service.PaymentProvider
import uz.murodjon.filemaster.config.AppProperties
import uz.murodjon.filemaster.exception.ForbiddenException
import uz.murodjon.filemaster.exception.NotFoundException
import java.time.Instant

@Service
class BillingServiceImpl(
    private val provider: PaymentProvider,
    private val users: UserRepository,
    private val props: AppProperties,
) : BillingService {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun checkout(user: User, request: CheckoutRequest): CheckoutResponse {
        val returnUrl = request.returnUrl ?: "http://localhost:3000/files"
        val url = provider.createCheckoutUrl(user, request.plan, returnUrl)
        log.info("Checkout started via '{}' for user {} -> {}", provider.name, user.id, request.plan)
        return CheckoutResponse(checkoutUrl = url)
    }

    @Transactional
    override fun handleWebhook(secret: String?, request: BillingWebhookRequest) {
        val expected = props.billing.webhookSecret
        if (expected.isBlank() || secret != expected) {
            throw ForbiddenException("Invalid webhook secret.")
        }
        val user = users.findById(request.userId).orElse(null)?.takeIf { it.active }
            ?: throw NotFoundException("User not found: ${request.userId}")

        val now = Instant.now().epochSecond
        // Extend from the current expiry when the same plan is still active, otherwise from now.
        val base = user.planExpiresTimestamp?.takeIf { it > now && user.plan == request.plan } ?: now
        user.plan = request.plan
        user.planExpiresTimestamp = base + request.days * 86400L
        user.updatedTimestamp = now
        users.save(user)
        log.info("Plan {} activated for user {} until {}", request.plan, user.id, user.planExpiresTimestamp)
    }
}

package uz.murodjon.filemaster.billing.service

import uz.murodjon.filemaster.auth.model.User
import uz.murodjon.filemaster.billing.dto.CheckoutRequest
import uz.murodjon.filemaster.billing.dto.CheckoutResponse

interface BillingService {
    fun checkout(user: User, request: CheckoutRequest): CheckoutResponse
}

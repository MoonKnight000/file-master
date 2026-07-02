package uz.murodjon.filemaster.billing.dto

import uz.murodjon.filemaster.auth.enums.UserPlan

data class CheckoutRequest(val plan: UserPlan, val returnUrl: String?)

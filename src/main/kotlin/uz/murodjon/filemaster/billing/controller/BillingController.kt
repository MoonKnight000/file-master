package uz.murodjon.filemaster.billing.controller

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import uz.murodjon.filemaster.auth.model.User
import uz.murodjon.filemaster.auth.security.CurrentUser
import uz.murodjon.filemaster.billing.dto.CheckoutRequest
import uz.murodjon.filemaster.billing.dto.CheckoutResponse
import uz.murodjon.filemaster.util.ResponseData

//TODO not implemented yet
@RequestMapping("/v1/billing")
interface BillingController {

    @PostMapping("/checkout")
    fun checkout(
        @RequestBody request: CheckoutRequest,
        @CurrentUser user: User,
    ): ResponseEntity<ResponseData<CheckoutResponse>>
}

package com.nest.nestplay.model

data class PaymentModel(
    val id: Long = 0,
    val payment_method_id: String = "",
    val status: String = "",
    val description: String = "",
    val qr_code_base64: String = "",
    val key_copy_past: String = "",
)

data class PaymentStatus(
    val success: Boolean?,
    val erro: Boolean?,
    val message: String?,
)
data class UserIdRequest(
    val userId: String
)

data class UserIdStatusRequest(
    val userId: String,
    val paymentId: Number
)

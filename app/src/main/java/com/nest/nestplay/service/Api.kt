package com.nest.nestplay.service
import com.nest.nestplay.model.PaymentModel
import com.nest.nestplay.model.PaymentStatus
import com.nest.nestplay.model.UserIdRequest
import com.nest.nestplay.model.UserIdStatusRequest
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Url

interface ApiService {
    @GET
    fun fetchDataM3u(@Url url: String): Call<String>
    @POST("/charge/create")
    fun generatePayment(@Body userId: UserIdRequest): Call<PaymentModel>

    @POST("/charge/payment-status")
    fun getStatusPayment(@Body userId: UserIdStatusRequest): Call<PaymentStatus>
}
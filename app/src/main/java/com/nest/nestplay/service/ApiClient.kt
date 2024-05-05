package com.nest.nestplay.service
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory

object ApiClient {

    private var retrofit: Retrofit? = null

    fun getApiClient(baseUrl: String): ApiService {
        if (retrofit == null || retrofit!!.baseUrl().toString() != baseUrl) {
            retrofit = Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(ScalarsConverterFactory.create())
                .build()
        }

        return retrofit!!.create(ApiService::class.java)
    }
}
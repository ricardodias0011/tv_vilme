package com.nest.nestplay.service
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.util.concurrent.TimeUnit
object ApiClient {

    private var retrofit: Retrofit? = null

    private const val BASE_URL = "https://apicharge.vilmeapp.com"
    fun NestApiClient(): ApiService {
        if (retrofit == null || retrofit!!.baseUrl().toString() != BASE_URL) {
            val httpClient = OkHttpClient.Builder()
                .connectTimeout(50, TimeUnit.SECONDS)
                .readTimeout(50, TimeUnit.SECONDS)
                .writeTimeout(50, TimeUnit.SECONDS)
                .build()

            retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(httpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }

        return retrofit!!.create(ApiService::class.java)
    }
    fun getApiClient(baseUrl: String): ApiService {
        if (retrofit == null || retrofit!!.baseUrl().toString() != baseUrl) {
            val httpClient = OkHttpClient.Builder()
                .connectTimeout(50, TimeUnit.SECONDS)
                .readTimeout(50, TimeUnit.SECONDS)
                .writeTimeout(50, TimeUnit.SECONDS)
                .build()

            retrofit = Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(httpClient) // Configurar o cliente HTTP personalizado
                .addConverterFactory(ScalarsConverterFactory.create())
                .build()
        }


        return retrofit!!.create(ApiService::class.java)
    }
}
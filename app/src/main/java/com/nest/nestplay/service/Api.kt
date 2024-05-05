package com.nest.nestplay.service
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Url

interface ApiService {
    @GET
    fun fetchDataM3u(@Url url: String): Call<String>
}
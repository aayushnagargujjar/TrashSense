package com.example.trashsense.AI_Eco_Dashboard.Forecast

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "https://forecast-app-hz6k.onrender.com/"

    val apiService: ForecastApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ForecastApiService::class.java)
    }
}

package com.example.trashsense.AI_Eco_Dashboard.Forecast

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

data class ForecastRequest(val uid: String)
data class ForecastResponse(val message: String)

interface ForecastApiService {
    @Headers("Content-Type: application/json")
    @POST("/predict")
    fun triggerForecast(@Body request: ForecastRequest): Call<ForecastResponse>
}


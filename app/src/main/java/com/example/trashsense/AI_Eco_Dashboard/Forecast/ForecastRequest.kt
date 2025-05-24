package com.example.trashsense.AI_Eco_Dashboard.Forecast


import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

data class ForecastRequest(val uid: String)

data class ForecastItem(
    val date: String,
    val co2_pred: Double,
    val water_pred: Double? = null
)

data class ForecastResponse(
    val message: String,
    val forecast: List<ForecastItem>?
)

interface ForecastApiService {
    @Headers("Content-Type: application/json")
    @POST("/predict")
    fun triggerForecast(@Body request: ForecastRequest): Call<ForecastResponse>
}
package com.example.trashsense.AI_Eco_Dashboard.Forecast

data class ForecastData(
    val date: String,
    val co2_pred: Double,
    val water_pred: Double? = null
)


data class ForecastResponse(
    val message: String,
    val forecast: List<ForecastData>?
)
data class ForecastRequest(
    val co2_data: List<SensorData>,
    val water_data: List<SensorData>
)

data class SensorData(
    val date: String,
    val value: Double
)


package com.example.trashsense.AI_Eco_Dashboard.Forecast

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.trashsense.R
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class Prediction : Fragment() {

    private lateinit var co2Chart: LineChart
    private lateinit var waterChart: LineChart
    private lateinit var progressBar: ProgressBar

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val TAG = "PredictionFragment"

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_prediction, container, false)

        co2Chart = view.findViewById(R.id.co2_forecast_chart)
        waterChart = view.findViewById(R.id.water_forecast_chart)
        progressBar = view.findViewById(R.id.loading_progress)
        progressBar.visibility =View.VISIBLE

        triggerForecastAPI()
        return view
    }

    private fun triggerForecastAPI() {


        val uid = auth.currentUser?.uid
        if (uid == null) {
            Toast.makeText(requireContext(), "User not logged in. Please log in to view predictions.", Toast.LENGTH_SHORT).show()
            progressBar.visibility = View.GONE
            return
        }

        db.collection("User").document(uid).collection("Timedata")
            .get()
            .addOnSuccessListener { documents ->
                val co2HistoryList = mutableListOf<SensorData>()
                val waterHistoryList = mutableListOf<SensorData>()
                val historicalChartData = mutableListOf<ForecastData>()

                for (document in documents) {
                    val timestampLong = document.getLong("timestamp")
                    if (timestampLong == null) {
                        Log.w(TAG, "Document ${document.id} missing 'timestamp' field. Skipping.")
                        continue
                    }
                    val dateString = convertTimestampToDateString(timestampLong)

                    val co2 = document.getDouble("co2_saved") ?: 0.0
                    val water = document.getDouble("water_saved") ?: 0.0

                    co2HistoryList.add(SensorData(dateString, co2))
                    waterHistoryList.add(SensorData(dateString, water))


                    historicalChartData.add(ForecastData(dateString, co2_pred = co2, water_pred = water))
                }

                Log.d(TAG, "Sending CO2 Historical Data: $co2HistoryList")
                Log.d(TAG, "Sending Water Historical Data: $waterHistoryList")

                val request = ForecastRequest(co2_data = co2HistoryList, water_data = waterHistoryList)

                RetrofitClient.apiService.triggerForecast(request)
                    .enqueue(object : Callback<ForecastResponse> {
                        override fun onResponse(
                            call: Call<ForecastResponse>,
                            response: Response<ForecastResponse>
                        ) {
                            progressBar.visibility = View.GONE
                            if (response.isSuccessful && response.body() != null) {
                                val forecastResponse = response.body()!!
                                val forecastList = forecastResponse.forecast ?: emptyList()

                                // ⭐ CRITICAL CHANGE: Pass historical and forecast data separately for different colors
                                updateForecastCharts(historicalChartData, forecastList)

                                Toast.makeText(requireContext(), "Forecast successful! 📈", Toast.LENGTH_SHORT).show()
                            } else {
                                val statusCode = response.code()
                                val errorBody = response.errorBody()?.string()
                                Toast.makeText(requireContext(), "Forecast failed (Code: $statusCode) 😔", Toast.LENGTH_LONG).show()
                                Log.e(TAG, "Forecast error. HTTP Status Code: $statusCode")
                                Log.e(TAG, "Error Body: $errorBody")
                            }
                        }

                        override fun onFailure(call: Call<ForecastResponse>, t: Throwable) {
                            progressBar.visibility = View.GONE
                            Toast.makeText(requireContext(), "Network error: ${t.message} 🌐", Toast.LENGTH_SHORT).show()
                            Log.e(TAG, "Forecast API call failed", t)
                        }
                    })

            }.addOnFailureListener { e ->
                progressBar.visibility = View.GONE
                Toast.makeText(requireContext(), "Failed to fetch historical data from Firestore. 😟", Toast.LENGTH_SHORT).show()
                Log.e(TAG, "Failed to fetch historical data from Firestore", e)
            }
    }

    private fun convertTimestampToDateString(timestamp: Long): String {
        val date = Date(timestamp)
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return formatter.format(date)
    }


    private fun updateForecastCharts(historicalData: List<ForecastData>, forecastData: List<ForecastData>) {
        // Combine all data to create a continuous set of labels for the X-axis
        val allData = historicalData + forecastData
        val labels = allData.map { it.date }

        val co2HistoryEntries = historicalData.mapIndexed { index, d -> Entry(index.toFloat(), d.co2_pred.toFloat()) }
        val co2ForecastEntries = forecastData.mapIndexed { index, d -> Entry((historicalData.size + index).toFloat(), d.co2_pred.toFloat()) }


        val co2HistoryDataSet = LineDataSet(co2HistoryEntries, "CO₂ History (kg)").apply {
            color = Color.GREEN
            setCircleColor(Color.GREEN)
            circleRadius = 4f
            setDrawValues(false)
            lineWidth = 2f
            mode = LineDataSet.Mode.CUBIC_BEZIER
        }


        val co2ForecastDataSet = LineDataSet(co2ForecastEntries, "CO₂ Forecast (kg)").apply {
            color = Color.parseColor("#8BC34A") // Light Green
            setCircleColor(Color.parseColor("#8BC34A"))
            circleRadius = 4f
            setDrawValues(true)
            lineWidth = 2f
            mode = LineDataSet.Mode.CUBIC_BEZIER
            enableDashedLine(10f, 5f, 0f)
        }


        val co2LineData = LineData(co2HistoryDataSet, co2ForecastDataSet)
        co2Chart.data = co2LineData
        setupChart(co2Chart, labels)
        co2Chart.invalidate()



        val waterHistoryEntries = historicalData.mapIndexed { index, d -> Entry(index.toFloat(), d.water_pred?.toFloat() ?: 0f) }

        val waterForecastEntries = forecastData.mapIndexed { index, d -> Entry((historicalData.size + index).toFloat(), d.water_pred?.toFloat() ?: 0f) }


        val waterHistoryDataSet = LineDataSet(waterHistoryEntries, "Water History (L)").apply {
            color = Color.BLUE
            setCircleColor(Color.BLUE)
            circleRadius = 4f
            setDrawValues(false)
            lineWidth = 2f
            mode = LineDataSet.Mode.CUBIC_BEZIER
        }

        // DataSet for forecast Water (e.g., a lighter blue)
        val waterForecastDataSet = LineDataSet(waterForecastEntries, "Water Forecast (L)").apply {
            color = Color.parseColor("#42A5F5") // Light Blue
            setCircleColor(Color.parseColor("#42A5F5"))
            circleRadius = 4f
            setDrawValues(true)
            lineWidth = 2f
            mode = LineDataSet.Mode.CUBIC_BEZIER
            enableDashedLine(10f, 5f, 0f)
        }

        val waterLineData = LineData(waterHistoryDataSet, waterForecastDataSet)
        waterChart.data = waterLineData
        setupChart(waterChart, labels)
        waterChart.invalidate()


        waterChart.visibility = if (allData.all { it.water_pred == null || it.water_pred == 0.0 }) View.GONE else View.VISIBLE
    }


    private fun setupChart(chart: LineChart, labels: List<String>) {
        chart.apply {
            description.isEnabled = false
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                valueFormatter = IndexAxisValueFormatter(labels)
                granularity = 1f
                labelRotationAngle = -45f

                labelCount = if (labels.size > 10) 5 else labels.size // Example: show max 5 labels if many
            }
            axisRight.isEnabled = false
            legend.isEnabled = true
            animateX(1000)
        }
    }

    private fun clearCharts() {
        co2Chart.clear()
        waterChart.clear()
        waterChart.visibility = View.VISIBLE
    }
}
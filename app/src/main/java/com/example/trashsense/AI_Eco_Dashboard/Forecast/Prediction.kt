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
import com.google.firebase.firestore.FirebaseFirestore
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class Prediction : Fragment() {

    private lateinit var co2Chart: LineChart
    private lateinit var waterChart: LineChart
    private lateinit var progressBar: ProgressBar

    private val db = FirebaseFirestore.getInstance()
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

        val addDataBtn = view.findViewById<Button>(R.id.add_data_btn)
        val predictBtn = view.findViewById<Button>(R.id.predict_btn)

        addDataBtn.setOnClickListener { addSampleDataToFirestore() }
        predictBtn.setOnClickListener { triggerForecastAPI() }

        fetchAndDisplayData("co2_data", "water_data", "Historical Data")

        return view
    }

    private fun triggerForecastAPI() {
        progressBar.visibility = View.VISIBLE

        val request = ForecastRequest(uid = "user123")
        RetrofitClient.apiService.triggerForecast(request)
            .enqueue(object : Callback<ForecastResponse> {
                override fun onResponse(call: Call<ForecastResponse>, response: Response<ForecastResponse>) {
                    progressBar.visibility = View.GONE
                    if (response.isSuccessful) {
                        Toast.makeText(requireContext(), "Forecast successful!", Toast.LENGTH_SHORT).show()
                        fetchAndDisplayData("user_forecast", "user_forecast", "Forecast Data")
                        Log.d(TAG, "Forecast result: ${response.body()?.message}")
                    } else {
                        val error = response.errorBody()?.string()
                        Toast.makeText(requireContext(), "Forecast failed: ${response.code()}", Toast.LENGTH_LONG).show()
                        Log.e(TAG, "API Error: ${response.code()} - $error")
                    }
                }

                override fun onFailure(call: Call<ForecastResponse>, t: Throwable) {
                    progressBar.visibility = View.GONE
                    Toast.makeText(requireContext(), "Forecast failed: ${t.message}", Toast.LENGTH_LONG).show()
                    Log.e(TAG, "Forecast error", t)
                }
            })
    }

    private fun addSampleDataToFirestore() {
        val co2Data = listOf(
            mapOf("date" to "2025-05-01", "value" to 4.5),
            mapOf("date" to "2025-05-02", "value" to 4.8),
            mapOf("date" to "2025-05-03", "value" to 5.0),
            mapOf("date" to "2025-05-04", "value" to 5.2),
            mapOf("date" to "2025-05-05", "value" to 5.1),
            mapOf("date" to "2025-05-06", "value" to 5.3),
            mapOf("date" to "2025-05-07", "value" to 5.4)
        )

        val waterData = listOf(
            mapOf("date" to "2025-05-01", "value" to 30.2),
            mapOf("date" to "2025-05-02", "value" to 31.0),
            mapOf("date" to "2025-05-03", "value" to 32.1),
            mapOf("date" to "2025-05-04", "value" to 31.7),
            mapOf("date" to "2025-05-05", "value" to 32.5),
            mapOf("date" to "2025-05-06", "value" to 33.0),
            mapOf("date" to "2025-05-07", "value" to 32.8)
        )

        db.collection("users").document("user123")
            .set(mapOf("co2_data" to co2Data, "water_data" to waterData))
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Sample data added", Toast.LENGTH_SHORT).show()
                fetchAndDisplayData("co2_data", "water_data", "Historical Data")
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Error adding data: ${e.message}", Toast.LENGTH_LONG).show()
                Log.e(TAG, "Firestore error", e)
            }
    }

    private fun fetchAndDisplayData(co2FieldName: String, waterFieldName: String, title: String) {
        db.collection("users").document("user123").get()
            .addOnSuccessListener { doc ->
                val co2DataList = doc.get(co2FieldName) as? List<Map<String, Any>>
                val waterDataList = if (co2FieldName == waterFieldName) co2DataList
                else doc.get(waterFieldName) as? List<Map<String, Any>>

                if (!co2DataList.isNullOrEmpty()) {
                    val co2Entries = mutableListOf<Entry>()
                    val waterEntries = mutableListOf<Entry>()
                    val dates = mutableListOf<String>()

                    val co2Key = if (co2FieldName == "user_forecast") "co2_pred" else "value"
                    val waterKey = if (waterFieldName == "user_forecast") "water_pred" else "value"

                    co2DataList.forEachIndexed { i, item ->
                        val date = item["date"] as? String ?: ""
                        val co2 = (item[co2Key] as? Number)?.toFloat() ?: 0f
                        val water = (waterDataList?.getOrNull(i)?.get(waterKey) as? Number)?.toFloat() ?: 0f
                        dates.add(date)
                        co2Entries.add(Entry(i.toFloat(), co2))
                        waterEntries.add(Entry(i.toFloat(), water))
                    }

                    updateCharts(co2Entries, waterEntries, dates, title)
                } else {
                    clearCharts()
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to load $title", Toast.LENGTH_LONG).show()
                clearCharts()
            }
    }

    private fun updateCharts(co2: List<Entry>, water: List<Entry>, labels: List<String>, title: String) {
        val co2DataSet = LineDataSet(co2, "CO₂ (kg) - $title").apply {
            color = Color.GREEN
            setCircleColor(Color.GREEN)
            circleRadius = 4f
            setDrawValues(true)
            lineWidth = 2f
            mode = LineDataSet.Mode.CUBIC_BEZIER
        }
        co2Chart.data = LineData(co2DataSet)
        setupChart(co2Chart, labels)
        co2Chart.invalidate()

        val waterDataSet = LineDataSet(water, "Water (L) - $title").apply {
            color = Color.BLUE
            setCircleColor(Color.BLUE)
            circleRadius = 4f
            setDrawValues(true)
            lineWidth = 2f
            mode = LineDataSet.Mode.CUBIC_BEZIER
        }
        waterChart.data = LineData(waterDataSet)
        setupChart(waterChart, labels)
        waterChart.invalidate()

        if (title == "Forecast Data" && water.all { it.y == 0f }) {
            waterChart.visibility = View.GONE
        } else {
            waterChart.visibility = View.VISIBLE
        }
    }

    private fun setupChart(chart: LineChart, labels: List<String>) {
        chart.apply {
            description.isEnabled = false
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                valueFormatter = IndexAxisValueFormatter(labels)
                granularity = 1f
                labelRotationAngle = -45f
                labelCount = labels.size
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

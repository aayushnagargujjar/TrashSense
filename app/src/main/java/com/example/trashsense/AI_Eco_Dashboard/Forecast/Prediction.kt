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
import com.google.firebase.auth.FirebaseAuth

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

        val addDataBtn = view.findViewById<Button>(R.id.add_data_btn)
        val predictBtn = view.findViewById<Button>(R.id.predict_btn)

        addDataBtn.setOnClickListener { addSampleDataToFirestore() }
        predictBtn.setOnClickListener { triggerForecastAPI() }

        fetchAndDisplayData("co2_data", "water_data", "Historical Data")

        return view
    }

    private fun triggerForecastAPI() {
        progressBar.visibility = View.VISIBLE


        val currentUserUid = auth.currentUser?.uid
        if (currentUserUid == null) {
            Toast.makeText(requireContext(), "User not logged in. Cannot make forecast.", Toast.LENGTH_LONG).show()
            progressBar.visibility = View.GONE
            return
        }


        val request = ForecastRequest(uid = currentUserUid)
        RetrofitClient.apiService.triggerForecast(request)
            .enqueue(object : Callback<ForecastResponse> {
                override fun onResponse(call: Call<ForecastResponse>, response: Response<ForecastResponse>) {
                    progressBar.visibility = View.GONE
                    if (response.isSuccessful) {
                        val forecastResponse = response.body()
                        Toast.makeText(requireContext(), forecastResponse?.message ?: "Forecast successful!", Toast.LENGTH_SHORT).show()
                        Log.d(TAG, "Forecast result: ${forecastResponse?.message}, Data: ${forecastResponse?.forecast}")


                        fetchAndDisplayData("user_forecast", "user_forecast", "Forecast Data")
                    } else {

                        val errorBody = response.errorBody()?.string()
                        val errorMessage = try {

                            val errorJson = org.json.JSONObject(errorBody)
                            errorJson.optString("error", "Unknown server error")
                        } catch (e: Exception) {

                            "Server error: ${response.code()} - $errorBody"
                        }
                        Toast.makeText(requireContext(), "Forecast failed: $errorMessage", Toast.LENGTH_LONG).show()
                        Log.e(TAG, "API Error: ${response.code()} - $errorBody")
                    }
                }

                override fun onFailure(call: Call<ForecastResponse>, t: Throwable) {
                    progressBar.visibility = View.GONE
                    Toast.makeText(requireContext(), "Network error: ${t.message}", Toast.LENGTH_LONG).show()
                    Log.e(TAG, "Forecast network error", t)
                }
            })
    }

    private fun addSampleDataToFirestore() {

        val currentUserUid = auth.currentUser?.uid
        if (currentUserUid == null) {
            Toast.makeText(requireContext(), "User not logged in. Cannot add sample data.", Toast.LENGTH_LONG).show()
            return
        }

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

        db.collection("users").document(currentUserUid) // Use actual UID
            .set(mapOf("co2_data" to co2Data, "water_data" to waterData))
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Sample data added for $currentUserUid", Toast.LENGTH_SHORT).show()
                fetchAndDisplayData("co2_data", "water_data", "Historical Data")
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Error adding data: ${e.message}", Toast.LENGTH_LONG).show()
                Log.e(TAG, "Firestore error", e)
            }
    }

    private fun fetchAndDisplayData(co2FieldName: String, waterFieldName: String, title: String) {
        val currentUserUid = auth.currentUser?.uid
        if (currentUserUid == null) {
            Log.w(TAG, "No user logged in, cannot fetch data.")
            clearCharts()
            return
        }

        db.collection("users").document(currentUserUid).get()
            .addOnSuccessListener { doc ->
                val co2DataList: List<Map<String, Any>>? = doc.get(co2FieldName) as? List<Map<String, Any>>
                val waterDataList: List<Map<String, Any>>? = if (co2FieldName == waterFieldName) {
                    co2DataList
                } else {
                    doc.get(waterFieldName) as? List<Map<String, Any>>
                }

                if (!co2DataList.isNullOrEmpty()) {
                    val co2Entries = mutableListOf<Entry>()
                    val waterEntries = mutableListOf<Entry>()
                    val dates = mutableListOf<String>()
                    val co2ValueKey = if (co2FieldName == "user_forecast") "co2_pred" else "value"
                    val waterValueKey = if (waterFieldName == "user_forecast") "water_pred" else "value"


                    co2DataList.forEachIndexed { i, item ->
                        val date = item["date"] as? String ?: ""
                        val co2 = (item[co2ValueKey] as? Number)?.toFloat() ?: 0f // Default to 0f if null
                        dates.add(date)
                        co2Entries.add(Entry(i.toFloat(), co2))
                        val water = (waterDataList?.getOrNull(i)?.get(waterValueKey) as? Number)?.toFloat() ?: 0f
                        waterEntries.add(Entry(i.toFloat(), water))
                    }

                    updateCharts(co2Entries, waterEntries, dates, title)
                } else {
                    clearCharts()
                    Toast.makeText(requireContext(), "No data found for '$title'", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Failed to load $title: ${e.message}", Toast.LENGTH_LONG).show()
                Log.e(TAG, "Firestore fetch error for $title", e)
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
            Log.d(TAG, "Water forecast data is all zero, hiding water chart.")
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
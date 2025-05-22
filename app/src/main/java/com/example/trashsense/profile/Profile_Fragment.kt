package com.example.trashsense.profile

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.trashsense.LoginActivity
import com.example.trashsense.R
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class Profile_Fragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private lateinit var co2Chart: LineChart
    private lateinit var waterChart: LineChart

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
    }

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile_, container, false)

        val pfImageView = view.findViewById<ImageView>(R.id.profile_pic)
        val pfUsername = view.findViewById<TextView>(R.id.pf_username)
        val ecotext = view.findViewById<TextView>(R.id.eco_A_number)
        val watertext = view.findViewById<TextView>(R.id.watersaved_number)
        val co2text = view.findViewById<TextView>(R.id.co2reduced_number)
        val logoutButton = view.findViewById<Button>(R.id.logout_btn)

        co2Chart = view.findViewById(R.id.co2_user_chart)
        waterChart = view.findViewById(R.id.water_user_chart)

        val uid = auth.currentUser?.uid
        if (uid != null) {
            db.collection("User").document(uid).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        pfUsername.text = document.getString("Username") ?: "Unknown"
                        ecotext.text = (document.getDouble("Eco_Activity") ?: 0.0).toInt().toString()
                        watertext.text = (document.getDouble("total_water_savings") ?: 0.0).toInt().toString()
                        co2text.text = (document.getDouble("total_co2_savings") ?: 0.0).toInt().toString()

                        val imageUrl = document.getString("url")
                        if (!imageUrl.isNullOrEmpty()) {
                            Glide.with(requireContext())
                                .load(imageUrl)
                                .circleCrop()
                                .into(pfImageView)
                        }


                        loadGraphData(uid)
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Failed to load profile.", Toast.LENGTH_SHORT).show()
                }
        }

        logoutButton.setOnClickListener {
            auth.signOut()
            startActivity(Intent(requireContext(), LoginActivity::class.java))
            requireActivity().finish()
        }

        return view
    }

    private fun loadGraphData(userId: String) {
        val co2Entries = mutableListOf<Entry>()
        val waterEntries = mutableListOf<Entry>()
        val labels = mutableListOf<String>()

        db.collection("User").document(userId)
            .collection("Timedata")
            .orderBy("timestamp")
            .get()
            .addOnSuccessListener { querySnapshot ->
                var index = 0
                for (doc in querySnapshot) {
                    val co2 = doc.getDouble("co2_saved")?.toFloat() ?: 0f
                    val water = doc.getDouble("water_saved")?.toFloat() ?: 0f
                    val timestamp = doc.getLong("timestamp") ?: 0L

                    val label = android.text.format.DateFormat.format("MM/dd", timestamp).toString()
                    labels.add(label)

                    co2Entries.add(Entry(index.toFloat(), co2))
                    waterEntries.add(Entry(index.toFloat(), water))
                    index++
                }

                setupDynamicChart(co2Chart, "CO₂ Saved", co2Entries, labels)
                setupDynamicChart(waterChart, "Water Saved", waterEntries, labels)
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to load graph data", Toast.LENGTH_SHORT).show()
            }
    }

    private fun setupDynamicChart(chart: LineChart, label: String, entries: List<Entry>, labels: List<String>) {
        val dataSet = LineDataSet(entries, label).apply {
            color = resources.getColor(R.color.teal_200, null)
            setDrawFilled(true)
            fillAlpha = 90
            valueTextSize = 10f
            mode = LineDataSet.Mode.CUBIC_BEZIER
            setCircleColor(resources.getColor(R.color.teal_700, null))
        }

        chart.apply {
            data = LineData(dataSet)
            description.isEnabled = false
            axisRight.isEnabled = false
            axisLeft.granularity = 1f
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                valueFormatter = IndexAxisValueFormatter(labels)
                granularity = 1f
            }
            invalidate()
        }
    }
}

package com.example.trashsense.AI_Eco_Dashboard.Co2Estimation

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.trashsense.AI_Eco_Dashboard.ValueorData_shower
import com.example.trashsense.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class Transport_Calculator : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var fb: FirebaseFirestore
    private lateinit var distanceEditText: EditText
    private lateinit var realTTextView: TextView
    private lateinit var insteadTTextView: TextView
    private lateinit var realTransportIcon: ImageView
    private lateinit var insteadTransportIcon: ImageView
    private lateinit var checksaving_tBtn: TextView

    private var realT: String? = null
    private var insteadT: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            realT = it.getString("realT")
            insteadT = it.getString("insteadT")

        }

        auth = FirebaseAuth.getInstance()
        fb = FirebaseFirestore.getInstance()
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_transport__calculator, container, false)


        realTTextView = view.findViewById(R.id.realTTextView)
        insteadTTextView = view.findViewById(R.id.insteadTTextView)
        distanceEditText = view.findViewById(R.id.distanceEditText)
        checksaving_tBtn = view.findViewById(R.id.checksaving_tBtn)
        realTransportIcon = view.findViewById(R.id.real_t_iconid)
        insteadTransportIcon = view.findViewById(R.id.instead_t_iconid)


        realTTextView.text = "$realT"
        insteadTTextView.text = "$insteadT"

        setTransportIcon(realTransportIcon, realT)
        setTransportIcon(insteadTransportIcon, insteadT)


        checksaving_tBtn.setOnClickListener {
            val kmStr = distanceEditText.text.toString()
            val distance = kmStr.toFloatOrNull()

            if (distance == null || distance <= 0) {
                Toast.makeText(requireContext(), "Enter a valid distance.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val realValues = getTransportValues(realT)
            val insteadValues = getTransportValues(insteadT)

            if (realValues == null || insteadValues == null) {
                Toast.makeText(requireContext(), "Invalid transport choices.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val co2SavedPerKm = insteadValues.first - realValues.first
            val waterSavedPerKm = insteadValues.second - realValues.second

            val exactCo2Saved = (co2SavedPerKm * distance).toInt()
            val exactWaterSaved = waterSavedPerKm * distance

            updateUserSavings(exactCo2Saved, exactWaterSaved)
        }

        return view
    }


    private fun setTransportIcon(imageView: ImageView, transportName: String?) {
        val iconResId = when (transportName?.lowercase()?.replace(" ", "")) {
            "walk" -> R.drawable.walk
            "cycle" -> R.drawable.cycle
            "train" -> R.drawable.ic_train
            "bus" -> R.drawable.ic_bus
            "motorbike" -> R.drawable.ic_motarbike
            "car" -> R.drawable.ic_car
            else -> R.drawable.ic_recycle
        }
        imageView.setImageResource(iconResId)
    }

    private fun getTransportValues(type: String?): Pair<Int, Float>? {
        return when (type?.lowercase()) {
            "car" -> Pair(166, 2.0f)
            "walk" -> Pair(0, 0.01f)
            "cycle" -> Pair(25, 0.05f)
            "train" -> Pair(35, 0.5f)
            "bus" -> Pair(93, 0.7f)
            "motorbike" -> Pair(90, 1.0f)
            else -> null
        }
    }

    private fun updateUserSavings(co2Saved: Int, waterSaved: Float) {
        val userId = auth.currentUser?.uid ?: run {
            Toast.makeText(requireContext(), "User not logged in.", Toast.LENGTH_SHORT).show()
            return
        }
        val userRef = fb.collection("User").document(userId)

        userRef.get().addOnSuccessListener { document ->
            val existingCO2 = document.getLong("total_co2_savings")?.toInt() ?: 0
            val existingWater = document.getDouble("total_water_savings")?.toFloat() ?: 0f

            val newCO2 = existingCO2 + co2Saved
            val newWater = existingWater + waterSaved

            val updateData = mapOf(
                "total_co2_savings" to newCO2,
                "total_water_savings" to newWater
            )

            userRef.set(updateData, SetOptions.merge())
                .addOnSuccessListener {
                    distanceEditText.text.clear()

                    val fragment = ValueorData_shower().apply {
                        arguments  = Bundle().apply {
                            putFloat("co2_value", co2Saved.toFloat())
                            putFloat("water_value", waterSaved)
                        }
                    }
                    requireActivity().supportFragmentManager.beginTransaction()
                        .replace(R.id.flFragment, fragment)
                        .commit()

                    Toast.makeText(requireContext(), "Savings updated!", Toast.LENGTH_SHORT).show()

                    logTimeSeriesData(co2Saved, waterSaved)
                }
                .addOnFailureListener { e ->
                    Toast.makeText(requireContext(), "Failed to update database: ${e.message}", Toast.LENGTH_SHORT).show()
                    android.util.Log.e("TransportCalculator", "Failed to update user savings", e)
                }
        }.addOnFailureListener { e ->
            Toast.makeText(requireContext(), "Failed to fetch user data: ${e.message}", Toast.LENGTH_SHORT).show()
            android.util.Log.e("TransportCalculator", "Failed to fetch user data", e)
        }
    }

    private fun logTimeSeriesData(co2Saved: Int, waterSaved: Float) {
        val userId = auth.currentUser?.uid ?: return
        val userRef = fb.collection("User").document(userId)

        val timeSeriesData = mapOf(
            "timestamp" to System.currentTimeMillis(),
            "co2_saved" to co2Saved,
            "water_saved" to waterSaved
        )

        userRef.collection("Timedata").document()
            .set(timeSeriesData)
            .addOnSuccessListener {
                android.util.Log.d("TransportCalculator", "Logged time series data for transport.")
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Failed to log time-series data.", Toast.LENGTH_SHORT).show()
                android.util.Log.e("TransportCalculator", "Failed to log time series data", e)
            }
    }
}
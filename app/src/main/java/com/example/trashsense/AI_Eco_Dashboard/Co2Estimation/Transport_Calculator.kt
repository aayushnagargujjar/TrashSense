package com.example.trashsense.AI_Eco_Dashboard.Co2Estimation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.trashsense.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class Transport_Calculator : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var fb: FirebaseFirestore
   private lateinit var distanceEditText:EditText
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_transport__calculator, container, false)

        val realTView = view.findViewById<TextView>(R.id.realTTextView)
        val insteadTView = view.findViewById<TextView>(R.id.insteadTTextView)
         distanceEditText = view.findViewById<EditText>(R.id.distanceEditText)
        val checksaving_tBtn = view.findViewById<TextView>(R.id.checksaving_tBtn)

        realTView.text = "Your transport: $realT"
        insteadTView.text = "Alternative option: $insteadT"

        checksaving_tBtn?.setOnClickListener {
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

            // Compute exact CO₂ and water savings
            val co2SavedPerKm = insteadValues.first - realValues.first
            val waterSavedPerKm = insteadValues.second - realValues.second

            val exactCo2Saved = (co2SavedPerKm * distance).toInt()
            val exactWaterSaved = waterSavedPerKm * distance

            updateUserSavings(exactCo2Saved, exactWaterSaved)

            Toast.makeText(
                requireContext(),
                "You saved $exactCo2Saved g CO₂ and %.2f L water!".format(exactWaterSaved),
                Toast.LENGTH_LONG
            ).show()
        }

        return view
    }

    private fun getTransportValues(type: String?): Pair<Int, Float>? {
        return when (type?.lowercase()) {
            "car" -> Pair(166, 2.0f)
            "walk" -> Pair(0, 0.01f)
            "cycle" -> Pair(25, 0.05f)
            "train" -> Pair(35, 0.5f)
            "bus" -> Pair(93, 0.7f)
            "bike" -> Pair(90, 1.0f)
            else -> null
        }
    }

    private fun updateUserSavings(co2Saved: Int, waterSaved: Float) {
        val userId = auth.currentUser?.uid ?: return

        val userRef = fb.collection("User").document(userId)
        userRef.get().addOnSuccessListener { document ->
            val existingCO2 = document.getLong("total_co2_savings")?.toInt() ?: 0
            val existingWater = document.getDouble("total_water_savings")?.toFloat() ?: 0f

            val newCO2 = existingCO2 + co2Saved
            val newWater = existingWater + waterSaved

            userRef.set(
                mapOf(
                    "total_co2_savings" to newCO2,
                    "total_water_savings" to newWater
                ), SetOptions.merge()
            ).addOnSuccessListener {
                distanceEditText.text.clear()
                Toast.makeText(requireContext(), "Savings updated!", Toast.LENGTH_SHORT).show()
            }.addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to update database.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

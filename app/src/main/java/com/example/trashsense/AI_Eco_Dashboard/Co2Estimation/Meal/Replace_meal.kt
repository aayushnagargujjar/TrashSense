package com.example.trashsense.AI_Eco_Dashboard.Co2Estimation.Meal

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.trashsense.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class Replace_meal : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private var realMeal: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        realMeal = arguments?.getString("realM")
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
    }

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_replace_meal, container, false)
        val titleText = view.findViewById<TextView>(R.id.title)

        titleText.text = "You chose $realMeal. Tap a better alternative!"

        val options = mapOf(
            R.id.Instead_Vegetable to "vegetables",
            R.id.Instead_fruit to "fruit",
            R.id.Instead_fish to "fish",
            R.id.Instead_chicken to "chicken",
            R.id.Instead_pork to "pork",
            R.id.Instead_beef to "beef"
        )

        options.forEach { (id, mealName) ->
            val layout = view.findViewById<LinearLayout>(id)
            layout.setOnClickListener {
                playAnimation(layout)
                calculateAndSave(realMeal, mealName)
            }
        }

        return view
    }

    private fun playAnimation(view: View) {
        val anim = AnimationUtils.loadAnimation(requireContext(), android.R.anim.fade_in)
        view.startAnimation(anim)
    }

    private fun getMealValues(meal: String?): Pair<Int, Float>? {
        return when (meal?.lowercase()) {
            "beef" -> Pair(27000, 15000f)
            "pork" -> Pair(12000, 6000f)
            "chicken" -> Pair(6000, 4300f)
            "fish" -> Pair(5000, 4000f)
            "vegetable" -> Pair(200, 300f)
            "fruit" -> Pair(300, 800f)
            else -> null
        }
    }

    private fun calculateAndSave(realMeal: String?, insteadMeal: String) {
        val realValues = getMealValues(realMeal)
        val insteadValues = getMealValues(insteadMeal)

        if (realValues == null || insteadValues == null) {
            Toast.makeText(requireContext(), "Invalid meal selection.", Toast.LENGTH_SHORT).show()
            return
        }

        val co2Saved = (realValues.first - insteadValues.first)
        val waterSaved = (realValues.second - insteadValues.second)

        val userId = auth.currentUser?.uid ?: return
        val userRef = db.collection("User").document(userId)

        userRef.get().addOnSuccessListener { doc ->
            val existingCO2 = doc.getLong("total_co2_savings")?.toInt() ?: 0
            val existingWater = doc.getDouble("total_water_savings")?.toFloat() ?: 0f

            val updatedCO2 = existingCO2 + co2Saved
            val updatedWater = existingWater + waterSaved

            userRef.set(
                mapOf(
                    "total_co2_savings" to updatedCO2,
                    "total_water_savings" to updatedWater
                ), SetOptions.merge()
            ).addOnSuccessListener {
                Toast.makeText(
                    requireContext(),
                    "You saved $co2Saved g CO₂ and %.2f L water!".format(waterSaved),
                    Toast.LENGTH_LONG
                ).show()
            }.addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to update savings.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

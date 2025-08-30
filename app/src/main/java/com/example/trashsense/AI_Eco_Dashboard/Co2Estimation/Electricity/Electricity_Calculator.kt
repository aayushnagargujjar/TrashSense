package com.example.trashsense.AI_Eco_Dashboard.Co2Estimation.Electricity

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.fragment.app.Fragment
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import com.example.trashsense.AI_Eco_Dashboard.ValueorData_shower
import com.example.trashsense.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions


class Electricity_Calculator : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var fb: FirebaseFirestore

    private lateinit var hoursEditText: EditText
    private lateinit var realApplianceTextView: TextView
    private lateinit var insteadApplianceTextView: TextView
    private lateinit var realApplianceIcon: ImageView
    private lateinit var insteadApplianceIcon: ImageView
    private lateinit var electricityResultTextView: TextView
    private lateinit var checkSavingButton: Button


    private var realApplianceName: String? = null
    private var insteadApplianceName: String? = null
    private var R_iconid: Int? = null
    private var I_iconid: Int? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

            R_iconid = it.getInt("realApplianceIconId", R.drawable.ic_fan)
            I_iconid = it.getInt("InsteadApplianceIconId", R.drawable.ic_ac)
            realApplianceName = it.getString("realAppliance")
            insteadApplianceName = it.getString("insteadAppliance")
        }

        auth = FirebaseAuth.getInstance()
        fb = FirebaseFirestore.getInstance()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_electricity__calculator, container, false)

        realApplianceTextView = view.findViewById(R.id.realApplianceText)
        insteadApplianceTextView = view.findViewById(R.id.insteadApplianceText)
        realApplianceIcon = view.findViewById(R.id.realApplianceIcon)
        insteadApplianceIcon = view.findViewById(R.id.insteadApplianceIcon)
        hoursEditText = view.findViewById(R.id.hoursEditText)
        checkSavingButton = view.findViewById(R.id.checksaving_eBtn)
        electricityResultTextView = view.findViewById(R.id.electricityResultTextView)


        realApplianceTextView.text = realApplianceName ?: "N/A"
        insteadApplianceTextView.text = insteadApplianceName ?: "N/A"


        setApplianceIcon(realApplianceIcon,realApplianceName)
        setApplianceIcon(insteadApplianceIcon,insteadApplianceName)


        checkSavingButton.setOnClickListener {
            calculateAndSaveChanges()
        }

        return view
    }

    private fun calculateAndSaveChanges() {
        val hoursStr = hoursEditText.text.toString()
        val hours = hoursStr.toFloatOrNull()

        if (hours == null || hours <= 0) {
            Toast.makeText(requireContext(), "Enter valid hours of usage.", Toast.LENGTH_SHORT).show()
            return
        }

        val realValues = getApplianceValues(realApplianceName)
        val insteadValues = getApplianceValues(insteadApplianceName)

        if (realValues == null || insteadValues == null) {
            Toast.makeText(requireContext(), "Invalid appliance choices.", Toast.LENGTH_SHORT).show()
            return
        }



        val co2SavedForPeriod = (insteadValues.co2gPerHour - realValues.co2gPerHour) * hours
        val waterSavedForPeriod = (insteadValues.waterLPerHour - realValues.waterLPerHour) * hours
        val unitsSavedForPeriod = (insteadValues.powerKw - realValues.powerKw) * hours


        updateUserSavings(co2SavedForPeriod, waterSavedForPeriod, unitsSavedForPeriod)

    }


    @OptIn(UnstableApi::class)
    private fun getApplianceValues(type: String?): ApplianceConsumption? {

        return when (type?.lowercase()?.replace(" ", "")?.replace("_", "")) {
            "bulb", "incandescentbulb", "standardbulb" -> ApplianceConsumption(51f, 0.12f, 0.060f)
            "led", "ledbulb", "ledlighting" -> ApplianceConsumption(8.5f, 0.02f, 0.010f) // 10W LED bulb
            "tubelight", "fluorescenttubelight" -> ApplianceConsumption(34f, 0.08f, 0.040f) // 40W Fluorescent tube
            "ledtubelight" -> ApplianceConsumption(15.3f, 0.036f, 0.018f) // 18W LED tube
            "tv", "television", "lcdtv" -> ApplianceConsumption(42.5f, 0.1f, 0.050f) // 50W LCD TV
            "ledtv" -> ApplianceConsumption(25.5f, 0.06f, 0.030f) // 30W LED TV
            "ac", "airconditioner" -> ApplianceConsumption(1275f, 3.0f, 1.500f) // 1500W AC
            "solar", "5starac" -> ApplianceConsumption(10f, .05f, .000f) //
            "fan", "ceilingfan" -> ApplianceConsumption(63.75f, 0.15f, 0.075f) // 75W Fan
            "efficientfan", "bldcfan" -> ApplianceConsumption(29.75f, 0.07f, 0.035f) // 35W BLDC Fan
            "cooler", "aircooler" -> ApplianceConsumption(170f, 0.4f, 0.200f) // 200W Air Cooler
            "refrigerator", "fridge" -> ApplianceConsumption(42.5f, 0.1f, 0.050f) // ~50W effective (e.g. 1.2 kWh/day)
            "efficientrefrigerator", "5starfridge" -> ApplianceConsumption(28.3f, 0.066f, 0.033f) // ~33W effective (e.g. 0.8 kWh/day)

            else -> {
                Log.w("ElectricityCalculator", "Unknown appliance type: $type")
                null
            }
        }
    }


    @SuppressLint("DefaultLocale")
    private fun updateUserSavings(co2SavedGrams: Float, waterSavedLiters: Float, unitsSavedKwh: Float) {
        val userId = auth.currentUser?.uid ?: return
        val userRef = fb.collection("User").document(userId)

        fb.runTransaction { transaction ->
            val snapshot = transaction.get(userRef)

            val existingCO2 = snapshot.getDouble("total_co2_savings")?.toLong() ?: 0L
            val existingWater = snapshot.getDouble("total_water_savings") ?: 0.0
            val existingUnits = snapshot.getDouble("total_electricity_savings") ?: 0.0

            val newCO2 = existingCO2 + co2SavedGrams.toLong()
            val newWater = existingWater + waterSavedLiters
            val newUnits = existingUnits + unitsSavedKwh

            val fragment = ValueorData_shower().apply {
                arguments  = Bundle().apply {

                    putFloat("co2_value", co2SavedGrams)
                    putFloat("water_value", waterSavedLiters)
                    putFloat("electricity_value", unitsSavedKwh)
                }}
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.flFragment,fragment)
                .commit()


            val updateData = mapOf(
                "total_co2_savings" to newCO2,
                "total_water_savings" to newWater,
                "total_electricity_savings" to newUnits
            )
            transaction.set(userRef, updateData, SetOptions.merge())


            null
        }.addOnSuccessListener {
            hoursEditText.text.clear()
            val resultText = String.format(
                "Savings: %.0fg CO₂, %.2fL Water, %.2f kWh Electricity!",
                co2SavedGrams, waterSavedLiters, unitsSavedKwh
            )
            electricityResultTextView.text = resultText
            electricityResultTextView.visibility = View.VISIBLE

            context?.let { Toast.makeText(it, "Savings updated!", Toast.LENGTH_SHORT).show()}
            logTimeSeriesData(co2SavedGrams.toInt(), waterSavedLiters )
        }.addOnFailureListener { e ->
            context?.let { Toast.makeText(it, "Failed to update savings: ${e.message}", Toast.LENGTH_LONG).show()}
            android.util.Log.e("ElectricityCalculator", "Failed to update user savings", e)
        }
    }

    @OptIn(UnstableApi::class)
    private fun logTimeSeriesData(co2Saved: Int, waterSaved: Float ) {
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

                Log.d("ElectricityCalculator", "Logged time series data for electricity.")
            }
            .addOnFailureListener { e ->

                context?.let { Toast.makeText(it, "Failed to log time-series data.", Toast.LENGTH_SHORT).show()}
                Log.e("ElectricityCalculator", "Failed to log time series data", e)
            }
    }


    private fun setApplianceIcon(imageView: ImageView, applianceName: String?) {
        val iconResId = when (applianceName?.lowercase()?.replace(" ", "")) {
          "solar" -> R.drawable.ic_solar
            "led", "ledbulb", "ledlighting" -> R.drawable.ic_led_bulb
            "ledtv"->R.drawable.ic_led
            "tubelight" -> R.drawable.ic_tubelight
            "tv" -> R.drawable.ic_tv
            "ac" -> R.drawable.ic_ac
            "fan" -> R.drawable.ic_fan
            "cooler" -> R.drawable.ic_cooler
            "refrigerator" -> R.drawable.ic_refrigerator
            else -> R.drawable.ic_recycle
        }
        imageView.setImageResource(iconResId)
    }


}
data class ApplianceConsumption(
    val co2gPerHour: Float,
    val waterLPerHour: Float,
    val powerKw: Float
)
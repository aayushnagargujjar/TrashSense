package com.example.trashsense.AI_Eco_Dashboard

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.cardview.widget.CardView
import com.example.trashsense.R
import androidx.fragment.app.FragmentManager
class ValueorData_shower : Fragment() {

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_valueor_data_shower, container, false)

        val co2Value = arguments?.getFloat("co2_value", 0.0f) ?: 0.0f
        val waterValue = arguments?.getFloat("water_value", 0.0f) ?: 0.0f
        val electricityValue = arguments?.getFloat("electricity_value", 0.0f) ?: 0.0f

        val cardCo2: CardView = view.findViewById(R.id.card_co2)
        val co2Text: TextView = view.findViewById(R.id.text_co2_value)
        val co2Reaction: TextView = view.findViewById(R.id.text_co2_reaction)

        val cardWater: CardView = view.findViewById(R.id.card_water)
        val waterText: TextView = view.findViewById(R.id.text_water_value)
        val waterReaction: TextView = view.findViewById(R.id.text_water_reaction)

        val cardElectricity: CardView = view.findViewById(R.id.card_electricity)
        val electricityText: TextView = view.findViewById(R.id.text_electricity_value)
        val electricityReaction: TextView = view.findViewById(R.id.text_electricity_reaction)

        val summaryText: TextView = view.findViewById(R.id.text_overall_summary)


        co2Text.text = String.format("%.1fg CO₂", co2Value)
        updateCard(cardCo2, co2Value, co2Reaction)

        waterText.text = String.format("%.2f L Water", waterValue)
        updateCard(cardWater, waterValue, waterReaction)

        if (electricityValue == 0.0f) {
            cardElectricity.visibility = View.GONE
        } else {
            cardElectricity.visibility = View.VISIBLE
            electricityText.text = String.format("%.2f kWh", electricityValue)
            updateCard(cardElectricity, electricityValue, electricityReaction)
        }

        val totalScore = co2Value + waterValue + electricityValue
        summaryText.text = when {
            totalScore < 50 -> "🌱 Decent effort! Let's aim for more savings next time."
            totalScore < 150 -> "🌿 You're doing great! Keep saving resources!"
            else -> "🏆 Eco-Champion! You're saving big for the planet!"
        }



        view.findViewById<Button>(R.id.continuebtn).setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.flFragment, Ai_Eco_Dashboard())
                .commit()
        }

        return view
    }

    private fun updateCard(card: CardView, value: Float, reactionText: TextView) {
        val messageText: TextView = when (card.id) {
            R.id.card_co2 -> card.findViewById(R.id.text_co2_message)
            R.id.card_water -> card.findViewById(R.id.text_water_message)
            R.id.card_electricity -> card.findViewById(R.id.text_electricity_message)
            else -> reactionText
        }

        val (highValueColor, lowValueColor) = when (card.id) {
            R.id.card_co2 -> Pair(Color.parseColor("#C8E6C9"), Color.parseColor("#FFCDD2"))
            R.id.card_water -> Pair(Color.parseColor("#B3E5FC"), Color.parseColor("#FFECB3"))
            R.id.card_electricity -> Pair(Color.parseColor("#FFF9C4"), Color.parseColor("#FFE0B2"))
            else -> Pair(Color.LTGRAY, Color.LTGRAY)
        }

        if (value >= 0) {
            card.setCardBackgroundColor(highValueColor)
            reactionText.text = "🎉"
        } else {
            card.setCardBackgroundColor(lowValueColor)
            reactionText.text = "⚠️"
        }

        messageText.text = when (card.id) {
            R.id.card_co2 -> when {
                value < 50 -> "🌿 A small step! Try to save more CO₂ next time."
                value < 150 -> "👍 Good job! Your CO₂ savings are growing."
                else -> "🏆 Amazing! Huge CO₂ savings — you're making a big difference!"
            }
            R.id.card_water -> when {
                value < 20 -> "💧 Minimal water saved. Let's aim higher!"
                value < 100 -> "🙂 Decent water savings. Keep going!"
                else -> "🚿 Incredible! You're conserving a lot of water!"
            }
            R.id.card_electricity -> when {
                value < 1 -> "🔌 Low electricity savings. Try switching off unused devices."
                value < 5 -> "⚡ Not bad! Keep up the conservation."
                else -> "💡 Great! You're saving a lot of energy!"
            }
            else -> ""
        }
    }
}

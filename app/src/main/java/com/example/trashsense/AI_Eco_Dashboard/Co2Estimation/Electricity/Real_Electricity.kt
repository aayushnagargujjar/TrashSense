package com.example.trashsense.AI_Eco_Dashboard.Co2Estimation.Electricity

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.LinearLayout
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import com.example.trashsense.R

class Real_Electricity : Fragment() {

    private lateinit var scaleAnim: Animation

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_real__electricity, container, false)
        scaleAnim = AnimationUtils.loadAnimation(requireContext(), R.anim.item_click)

        val electricityOptions = mapOf(
            view.findViewById<CardView>(R.id.card_solar) to "solar",
            view.findViewById<CardView>(R.id.card_led) to "led",
            view.findViewById<CardView>(R.id.card_fan) to "fan",
            view.findViewById<CardView>(R.id.card_tubelight) to "tubelight",
            view.findViewById<CardView>(R.id.card_ledbulb) to "ledbulb",
            view.findViewById<CardView>(R.id.card_tv) to "tv",
            view.findViewById<CardView>(R.id.card_ac) to "ac",
            view.findViewById<CardView>(R.id.card_cooler) to "cooler",
            view.findViewById<CardView>(R.id.card_fridge) to "fridge"
        )

        electricityOptions.forEach { (card, applianceType) ->
            card.setOnClickListener {
                card.startAnimation(scaleAnim)

                scaleAnim.setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationStart(animation: Animation?) {}

                    override fun onAnimationEnd(animation: Animation?) {

                        val currentContext = context
                        val currentResources = resources

                        if (currentContext != null) {
                            val id = currentResources.getIdentifier("card_${applianceType}", "drawable", currentContext.packageName)

                            val fragment = Instead_electricity().apply {
                                arguments = Bundle().apply {
                                    putString("realAppliance", applianceType)
                                    putInt("realApplianceIconId", id)
                                }
                            }

                            requireActivity().supportFragmentManager.beginTransaction()
                                .replace(R.id.flFragment, fragment)
                                .addToBackStack(null)
                                .commit()
                        }
                    }

                    override fun onAnimationRepeat(animation: Animation?) {}
                })
            }
        }

        return view
    }
}
package com.example.trashsense.AI_Eco_Dashboard.Co2Estimation.Meal

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

class MealFragment : Fragment() {

    private lateinit var scaleAnim: Animation

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_meal, container, false)

        // Load animation
        scaleAnim = AnimationUtils.loadAnimation(requireContext(), R.anim.item_click)

        // Meal options (using LinearLayouts inside CardViews)
        val mealOptions = mapOf(
            view.findViewById<CardView>(R.id.card_breakfast) to "Breakfast",
            view.findViewById<CardView>(R.id.card_brunch) to "Brunch",
            view.findViewById<CardView>(R.id.card_lunch) to "Lunch",
            view.findViewById<CardView>(R.id.card_snack) to "Snack",
            view.findViewById<CardView>(R.id.card_dinner) to "Dinner",
            view.findViewById<CardView>(R.id.card_meal) to "Meal"
        )

        // Set click listeners with animation
        mealOptions.forEach { (layout, meal) ->
            layout.setOnClickListener {
                layout.startAnimation(scaleAnim)

                scaleAnim.setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationStart(animation: Animation?) {}

                    override fun onAnimationEnd(animation: Animation?) {
                        val fragment = Real_meal().apply {
                            arguments = Bundle().apply {
                                putString("mealType", meal)
                            }
                        }

                        requireActivity().supportFragmentManager.beginTransaction()
                            .replace(R.id.flFragment, fragment)
                            .addToBackStack(null)
                            .commit()
                    }

                    override fun onAnimationRepeat(animation: Animation?) {}
                })
            }
        }

        return view
    }
}

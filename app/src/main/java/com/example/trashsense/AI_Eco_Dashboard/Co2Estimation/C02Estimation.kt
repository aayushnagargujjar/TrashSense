package com.example.trashsense.AI_Eco_Dashboard.Co2Estimation

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.LinearLayout
import com.example.trashsense.AI_Eco_Dashboard.Co2Estimation.Meal.MealFragment
import com.example.trashsense.R

class C02Estimation : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_c02_estimation, container, false)

        // Load animation
        val scaleAnim = AnimationUtils.loadAnimation(requireContext(), R.anim.item_click)

        // Transport option click listener
        val transport = view.findViewById<LinearLayout>(R.id.transportOption)
        transport.setOnClickListener {
            transport.startAnimation(scaleAnim) // Start animation

            scaleAnim.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation?) {}

                override fun onAnimationEnd(animation: Animation?) {
                    val tFragment = Choose_Transport()
                    requireActivity().supportFragmentManager.beginTransaction()
                        .replace(R.id.flFragment, tFragment)
                        .addToBackStack(null)
                        .commit()
                }

                override fun onAnimationRepeat(animation: Animation?) {}
            })
        }


        val meal = view.findViewById<LinearLayout>(R.id.mealOption)
        meal.setOnClickListener {
            meal.startAnimation(scaleAnim)

            scaleAnim.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation?) {}

                override fun onAnimationEnd(animation: Animation?) {
                    val tFragment = MealFragment()
                    requireActivity().supportFragmentManager.beginTransaction()
                        .replace(R.id.flFragment, tFragment)
                        .addToBackStack(null)
                        .commit()
                }

                override fun onAnimationRepeat(animation: Animation?) {}
            })
        }

        return view
    }
}

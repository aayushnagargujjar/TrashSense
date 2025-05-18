package com.example.trashsense.AI_Eco_Dashboard.Co2Estimation.Meal

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.LinearLayout
import com.example.trashsense.AI_Eco_Dashboard.Co2Estimation.Instedof_Transport
import com.example.trashsense.R


class Real_meal : Fragment() {
    private lateinit var scaleAnim: Animation

        @SuppressLint("MissingInflatedId")
        override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View {
            val view = inflater.inflate(R.layout.fragment_real_meal, container, false)

            // Load animation
            scaleAnim = AnimationUtils.loadAnimation(requireContext(), R.anim.item_click)

            // Meal options
            val mealOptions = mapOf(
                view.findViewById<LinearLayout>(R.id.Real_fruit) to "fruit",
                view.findViewById<LinearLayout>(R.id.Real_vegetable) to "vegetable",
                view.findViewById<LinearLayout>(R.id.Real_beef) to "beef",
                view.findViewById<LinearLayout>(R.id.Real_fish) to "fish",
                view.findViewById<LinearLayout>(R.id.Real_pork) to "pork",
                view.findViewById<LinearLayout>(R.id.Real_chicken) to "chicken"
            )


            mealOptions.forEach { (layout, mealType) ->
                layout.setOnClickListener {
                    layout.startAnimation(scaleAnim)

                    scaleAnim.setAnimationListener(object : Animation.AnimationListener {
                        override fun onAnimationStart(animation: Animation?) {}

                        override fun onAnimationEnd(animation: Animation?) {
                            val fragment = Replace_meal().apply {
                                arguments = Bundle().apply {
                                    putString("realM", mealType)
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

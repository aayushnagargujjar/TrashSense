package com.example.trashsense.AI_Eco_Dashboard.Co2Estimation.Electricity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import com.example.trashsense.R

class Instead_electricity : Fragment() {

    private var realAppliance: String? = null
    private lateinit var scaleAnim: Animation

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            realAppliance = it.getString("realAppliance")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_instead_electricity, container, false)

        scaleAnim = AnimationUtils.loadAnimation(requireContext(), R.anim.item_click)

        val alternativeOptions = mapOf(
            view.findViewById<LinearLayout>(R.id.Icard_solar) to "solar",
            view.findViewById<LinearLayout>(R.id.Icard_led) to "led",
            view.findViewById<LinearLayout>(R.id.Icard_fan) to "fan",
            view.findViewById<LinearLayout>(R.id.Icard_tubelight) to "tubelight",
            view.findViewById<LinearLayout>(R.id.Icard_ledbulb) to "ledbulb",
            view.findViewById<LinearLayout>(R.id.Icard_tv) to "tv",
            view.findViewById<LinearLayout>(R.id.Icard_ac) to "ac",
            view.findViewById<LinearLayout>(R.id.Icard_cooler) to "cooler",
            view.findViewById<LinearLayout>(R.id.Icard_fridge) to "fridge"
        )

        alternativeOptions.forEach { (layout, insteadAppliance) ->
            layout.setOnClickListener {
                layout.startAnimation(scaleAnim)

                scaleAnim.setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationStart(animation: Animation?) {}

                    override fun onAnimationEnd(animation: Animation?) {
                        val fragment = Electricity_Calculator().apply {
                            arguments = Bundle().apply {
                                putString("realAppliance", realAppliance)
                                putString("insteadAppliance", insteadAppliance)
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

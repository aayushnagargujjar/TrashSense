package com.example.trashsense.AI_Eco_Dashboard.Co2Estimation.Electricity

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

class Instead_electricity : Fragment() {

    private var realAppliance: String? = null
    private var realApplianceid: Int? = null
    private lateinit var scaleAnim: Animation

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            realApplianceid =it.getInt("realApplianceIconId")
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
            view.findViewById<CardView>(R.id.Icard_solar) to "solar",
            view.findViewById<CardView>(R.id.Icard_led) to "led",
            view.findViewById<CardView>(R.id.Icard_fan) to "fan",
            view.findViewById<CardView>(R.id.Icard_tubelight) to "tubelight",
            view.findViewById<CardView>(R.id.Icard_ledbulb) to "ledbulb",
            view.findViewById<CardView>(R.id.Icard_tv) to "tv",
            view.findViewById<CardView>(R.id.Icard_ac) to "ac",
            view.findViewById<CardView>(R.id.Icard_cooler) to "cooler",
            view.findViewById<CardView>(R.id.Icard_fridge) to "fridge"
        )

        alternativeOptions.forEach { (layout, insteadAppliance) ->
            layout.setOnClickListener {
                layout.startAnimation(scaleAnim)

                scaleAnim.setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationStart(animation: Animation?) {}

                    override fun onAnimationEnd(animation: Animation?) {

                        val currentContext = context
                        val currentResources = resources

                        if (currentContext != null) {
                            val id = currentResources.getIdentifier("Icard_${insteadAppliance}", "drawable", currentContext.packageName)

                            val fragment = Electricity_Calculator().apply {
                                arguments = Bundle().apply {
                                    putString("realAppliance", realAppliance)
                                    putString("insteadAppliance", insteadAppliance)
                                    realApplianceid?.let { it1 -> putInt("realApplianceIconId", it1) }
                                    putInt("InsteadApplianceIconId", id)
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
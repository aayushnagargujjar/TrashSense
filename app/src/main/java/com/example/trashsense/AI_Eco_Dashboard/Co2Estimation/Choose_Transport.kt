package com.example.trashsense.AI_Eco_Dashboard.Co2Estimation

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.LinearLayout
import com.example.trashsense.R

class Choose_Transport : Fragment() {

    private lateinit var scaleAnim: Animation

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_choose__transport, container, false)


        scaleAnim = AnimationUtils.loadAnimation(requireContext(), R.anim.item_click)


        val transportOptions = mapOf(
            view.findViewById<LinearLayout>(R.id.realT_walk) to "walk",
            view.findViewById<LinearLayout>(R.id.realT_cycle) to "cycle",
            view.findViewById<LinearLayout>(R.id.realT_train) to "train",
            view.findViewById<LinearLayout>(R.id.realT_bus) to "bus",
            view.findViewById<LinearLayout>(R.id.realT_motorbike) to "motorbike",
            view.findViewById<LinearLayout>(R.id.realT_car) to "car"
        )


        transportOptions.forEach { (view, transportType) ->
            view.setOnClickListener {
                view.startAnimation(scaleAnim)

                scaleAnim.setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationStart(animation: Animation?) {}

                    override fun onAnimationEnd(animation: Animation?) {
                        val fragment = Instedof_Transport().apply {
                            arguments = Bundle().apply {
                                putString("realT", transportType)
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

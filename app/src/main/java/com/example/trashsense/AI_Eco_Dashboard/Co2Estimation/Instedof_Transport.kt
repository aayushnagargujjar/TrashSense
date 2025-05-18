package com.example.trashsense.AI_Eco_Dashboard.Co2Estimation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import com.example.trashsense.R

class Instedof_Transport : Fragment() {

    private var realT: String? = null
    private lateinit var scaleAnim: Animation

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        arguments?.let {
            realT = it.getString("realT")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_instedof__transport, container, false)


        scaleAnim = AnimationUtils.loadAnimation(requireContext(), R.anim.item_click)


        val transportOptions = mapOf(
            view.findViewById<LinearLayout>(R.id.InsteadT_walk) to "walk",
            view.findViewById<LinearLayout>(R.id.InsteadT_cycle) to "cycle",
            view.findViewById<LinearLayout>(R.id.InsteadT_train) to "train",
            view.findViewById<LinearLayout>(R.id.InsteadT_bus) to "bus",
            view.findViewById<LinearLayout>(R.id.InsteadT_motorbike) to "motorbike",
            view.findViewById<LinearLayout>(R.id.InsteadT_car) to "car"
        )

        transportOptions.forEach { (layout, insteadT) ->
            layout.setOnClickListener {
                layout.startAnimation(scaleAnim)

                scaleAnim.setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationStart(animation: Animation?) {}

                    override fun onAnimationEnd(animation: Animation?) {
                        val fragment = Transport_Calculator().apply {
                            arguments = Bundle().apply {
                                putString("realT", realT)
                                putString("insteadT", insteadT)
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

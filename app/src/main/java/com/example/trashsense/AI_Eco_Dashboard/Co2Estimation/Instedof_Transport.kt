package com.example.trashsense.AI_Eco_Dashboard.Co2Estimation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import com.example.trashsense.R

class Instedof_Transport : Fragment() {

    private var realT: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Get the realT argument (transport originally intended)
        arguments?.let {
            realT = it.getString("realT")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_instedof__transport, container, false)

        // Map of insteadT choices
        val transports = mapOf(
            R.id.InsteadT_walk to "walk",
            R.id.InsteadT_cycle to "cycle",
            R.id.InsteadT_train to "train",
            R.id.InsteadT_bus to "bus",
            R.id.InsteadT_motorbike to "motorbike",
            R.id.InsteadT_car to "car"
        )

        for ((id, insteadT) in transports) {
            val layout = view.findViewById<LinearLayout>(id)
            layout.setOnClickListener {

                val fragment = Transport_Calculator()
                val bundle = Bundle().apply {
                    putString("realT", realT)
                    putString("insteadT", insteadT)
                }
                fragment.arguments = bundle

                requireActivity().supportFragmentManager.beginTransaction()
                    .replace(R.id.flFragment, fragment)
                    .addToBackStack(null)
                    .commit()
            }
        }

        return view
    }
}

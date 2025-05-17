package com.example.trashsense.AI_Eco_Dashboard.Co2Estimation

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.example.trashsense.R

class Choose_Transport : Fragment() {

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_choose__transport, container, false)

        val walk = view.findViewById<LinearLayout>(R.id.realT_walk)
        val cycle = view.findViewById<LinearLayout>(R.id.realT_cycle)
        val train = view.findViewById<LinearLayout>(R.id.realT_train)
        val bus = view.findViewById<LinearLayout>(R.id.realT_bus)
        val motorbike = view.findViewById<LinearLayout>(R.id.realT_motorbike)
        val car = view.findViewById<LinearLayout>(R.id.realT_car)

        setTransportClick(walk, "walk")
        setTransportClick(cycle, "cycle")
        setTransportClick(train, "train")
        setTransportClick(bus, "bus")
        setTransportClick(motorbike, "motorbike")
        setTransportClick(car, "car")

        return view
    }

    private fun setTransportClick(view: View, transportType: String) {
        view.setOnClickListener {
            val fragment = Instedof_Transport()
            val bundle = Bundle()
            bundle.putString("realT", transportType)
            fragment.arguments = bundle

            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.flFragment, fragment)
                .addToBackStack(null)
                .commit()
        }
    }
}

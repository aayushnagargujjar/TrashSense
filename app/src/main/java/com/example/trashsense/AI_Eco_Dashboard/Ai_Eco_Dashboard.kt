package com.example.trashsense.AI_Eco_Dashboard

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.example.trashsense.AI_Eco_Dashboard.Co2Estimation.C02Estimation
import com.example.trashsense.R


class Ai_Eco_Dashboard : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var view =inflater.inflate(R.layout.fragment_ai__eco__dashboard, container, false)
        // Inflate the layout for this fragment
        var co2 =view.findViewById<Button>(R.id.btnCo2)
        co2.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.flFragment,C02Estimation())
                .addToBackStack(null)
                .commit()
        }
        return view
    }



}
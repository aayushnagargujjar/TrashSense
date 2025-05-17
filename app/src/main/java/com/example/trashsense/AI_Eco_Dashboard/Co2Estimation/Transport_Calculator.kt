package com.example.trashsense.AI_Eco_Dashboard.Co2Estimation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.trashsense.R

class Transport_Calculator : Fragment() {

    private var realT: String? = null
    private var insteadT: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            realT = it.getString("realT")
            insteadT = it.getString("insteadT")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_transport__calculator, container, false)

        // Optionally show the values in TextViews if needed
        val realTView = view.findViewById<TextView>(R.id.realTTextView)
        val insteadTView = view.findViewById<TextView>(R.id.insteadTTextView)

        realTView?.text = "Intended Transport: $realT"
        insteadTView?.text = "Chosen Greener Transport: $insteadT"

        return view
    }
}

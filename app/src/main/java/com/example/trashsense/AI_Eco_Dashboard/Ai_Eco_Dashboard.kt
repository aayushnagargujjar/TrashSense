package com.example.trashsense.AI_Eco_Dashboard

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import com.example.trashsense.AI_Eco_Dashboard.Co2Estimation.C02Estimation
import com.example.trashsense.AI_Eco_Dashboard.Forecast.Prediction
import com.example.trashsense.AI_Eco_Dashboard.wastesorting.Waste_Sortingfragment
import com.example.trashsense.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


class Ai_Eco_Dashboard : Fragment() {
    private lateinit var db :FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var view =inflater.inflate(R.layout.fragment_ai__eco__dashboard, container, false)
        db = FirebaseFirestore.getInstance()
        auth =FirebaseAuth.getInstance()
        // Inflate the layout for this fragment
        var co2 =view.findViewById<Button>(R.id.btnCo2)
        co2.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.flFragment,C02Estimation())
                .addToBackStack(null)
                .commit()
        }
        var scan = view.findViewById<Button>(R.id.btnSortWaste)
        scan.setOnClickListener {
            Fragmentset(Waste_Sortingfragment())
        }
        view.findViewById<Button>(R.id.btnForecast).setOnClickListener{
            db.collection("User").document(auth.currentUser?.uid.toString()).collection("Timedata").get()
                .addOnSuccessListener { documents->

                    if( documents.size() >=5){
                        Toast.makeText(requireActivity(),"Number of documents: ${documents.size()}",Toast.LENGTH_SHORT).show()
                        Fragmentset(Prediction())

                    }
                    else
                    {
                        Toast.makeText(requireActivity()," Minimum 5 data Required:you have ${documents.size()} data ",Toast.LENGTH_SHORT).show()
                    }

                }

        }

        return view
    }

  private fun Fragmentset(fragment: Fragment){
      requireActivity().supportFragmentManager.beginTransaction()
          .replace(R.id.flFragment,fragment)
          .addToBackStack(null)
          .commit()
  }

}
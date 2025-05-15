package com.example.trashsense.Question

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.trashsense.HomeActivity
import com.example.trashsense.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class Question3 : AppCompatActivity() {

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_question3)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val plastic = findViewById<CheckBox>(R.id.goalPlastic)
        val energy = findViewById<CheckBox>(R.id.goalEnergy)
        val carbon = findViewById<CheckBox>(R.id.goalCarbon)
        val btnShow = findViewById<Button>(R.id.btnShowResults)

        btnShow.setOnClickListener {

            val interests = intent.getStringArrayListExtra("interests") ?: arrayListOf()

            val location = intent.getStringExtra("location") ?: ""


            val selectedGoals = mutableListOf<String>()
            if (plastic.isChecked) selectedGoals.add("Home Improvement")
            if (energy.isChecked) selectedGoals.add("Shopping Habits")
            if (carbon.isChecked) selectedGoals.add("Community")


            val ecoActions = getEcoActions(interests, location, selectedGoals)


            val userId = FirebaseAuth.getInstance().currentUser?.uid
            if (userId != null) {
                saveEcoActionsToFirestore(userId, ecoActions)
            } else {
                Toast.makeText(this, "User not logged in!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }


            val intent = Intent(this, HomeActivity::class.java)
            intent.putStringArrayListExtra("ecoActions", ArrayList(ecoActions))
            startActivity(intent)
            finish()
        }
    }

    private fun saveEcoActionsToFirestore(userId: String, actions: List<String>) {
        val db = FirebaseFirestore.getInstance()
        val data = hashMapOf(
            "ecoActions" to actions,
            "timestamp" to FieldValue.serverTimestamp()
        )
        db.collection("User").document(userId)
            .set(data, SetOptions.merge())
            .addOnSuccessListener {
                Toast.makeText(this, "Your Eco Action store ", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to save data: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // Generate eco actions based on all answers
    private fun getEcoActions(
        interests: List<String>,
        location: String,
        goals: List<String>
    ): List<String> {
        val ecoActions = mutableSetOf<String>()


        for (interest in interests) {
            when (interest) {
                "gardening" -> {
                   // ecoActions.add("Start composting kitchen waste — Good for soil and reduces landfill")
                    ecoActions.add("Grow your own herbs — Saves water and reduces food miles")
                }
                "vegan" -> {
                   // ecoActions.add("Eat more plant-based meals — Reduce greenhouse gases")
                    ecoActions.add("Buy local and organic produce — Support sustainable farming")
                }
                "clean_transport" -> {
                    ecoActions.add("Bike or walk for short trips — Reduce carbon footprint")
                    //ecoActions.add("Use public transport or carpool — Save fuel and money")
                }
                "zero_waste" -> {
                    //ecoActions.add("Use reusable bags — Cut down plastic waste")
                    ecoActions.add("Avoid single-use plastics — Choose reusable alternatives")
                }
            }
        }


        when (location) {
            "urban" -> ecoActions.add("Participate in local cleanups — Make your neighborhood greener")
            "suburban" -> ecoActions.add("Support farmers markets — Build community and reduce packaging")
            "rural" -> ecoActions.add("Install solar panels — Use renewable energy")
        }


        for (goal in goals) {
            when (goal) {
                "Home Improvement" -> {
                    ecoActions.add("Insulate your home — Save heating and cooling energy")
                    //ecoActions.add("Install LED bulbs — Save energy at home")
                }
                "Shopping Habits" -> {
                 //   ecoActions.add("Choose eco-friendly products — Support green businesses")
                    ecoActions.add("Switch to bamboo toothbrush — Zero-waste starter")
                }
                "Community" -> {
                    ecoActions.add("Organize a community bike day — Promote eco-friendly transport")
                   // ecoActions.add("Participate in local cleanups — Make your neighborhood greener")
                }
            }
        }

        return ecoActions.take(4).toList() // limit to .... max, unique by using set
    }
}

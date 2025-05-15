package com.example.trashsense.Question

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.RadioGroup
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.trashsense.R

class Question2 : AppCompatActivity() {

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_question2)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val radioGroup = findViewById<RadioGroup>(R.id.locationGroup)
        val nextBtn = findViewById<Button>(R.id.btnNextLocation)

        // Receive Q1 interests passed from Question1
        val interests = intent.getStringArrayListExtra("interests") ?: arrayListOf()

        nextBtn.setOnClickListener {
            val selectedId = radioGroup.checkedRadioButtonId
            if (selectedId == -1) {
                Toast.makeText(this, "Please select an option", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val location = when (selectedId) {
                R.id.radioUrban -> "urban"
                R.id.radioSuburban -> "suburban"
                R.id.radioRural -> "rural"
                else -> "unknown"
            }

            // Pass both Q1 and Q2 data to Question3
            val intent = Intent(this, Question3::class.java)
            intent.putStringArrayListExtra("interests", interests)
            intent.putExtra("location", location)
            startActivity(intent)
        }
    }
}

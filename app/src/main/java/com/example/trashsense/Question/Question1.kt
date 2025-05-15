package com.example.trashsense.Question

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.trashsense.R

class Question1 : AppCompatActivity() {

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_question1)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val gardening = findViewById<CheckBox>(R.id.optionGardening)
        val vegan = findViewById<CheckBox>(R.id.optionVegan)
        val transport = findViewById<CheckBox>(R.id.optionTransport)
        val waste = findViewById<CheckBox>(R.id.optionWaste)
        val nextBtn = findViewById<Button>(R.id.btnNext)

        nextBtn.setOnClickListener {
            val selectedOptions = mutableListOf<String>()
            if (gardening.isChecked) selectedOptions.add("gardening")
            if (vegan.isChecked) selectedOptions.add("vegan")
            if (transport.isChecked) selectedOptions.add("clean_transport")
            if (waste.isChecked) selectedOptions.add("zero_waste")

            val intent = Intent(this, Question2::class.java)
            intent.putStringArrayListExtra("interests", ArrayList(selectedOptions))
            startActivity(intent)
        }
    }
}

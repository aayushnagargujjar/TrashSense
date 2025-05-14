package com.example.trashsense

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore

class SignUpActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var db: FirebaseFirestore

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_sign_up)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        auth = FirebaseAuth.getInstance()
        firebaseDatabase = FirebaseDatabase.getInstance()
        db = FirebaseFirestore.getInstance()


        val mail = findViewById<TextInputEditText>(R.id.gmail_signup_id)
        val passwordField = findViewById<TextInputEditText>(R.id.password_signup)
        val repasswordField = findViewById<TextInputEditText>(R.id.repassword_signup)
        val signupBtn = findViewById<Button>(R.id.signup_btn)
        val already_acc= findViewById<TextView>(R.id.already_acc_btn)

        already_acc.setOnClickListener{
            startActivity(Intent(this,LoginActivity::class.java))
        }

        signupBtn.setOnClickListener {
            val emailText = mail.text?.toString()?.trim() ?: ""
            val passwordText = passwordField.text?.toString()?.trim() ?: ""
            val repasswordText = repasswordField.text?.toString()?.trim() ?: ""

            if (emailText.isNotEmpty() && passwordText.isNotEmpty() && repasswordText.isNotEmpty()) {
                if (passwordText == repasswordText) {
                    registerUser(emailText, passwordText)
                } else {
                    Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun registerUser(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val uid = auth.currentUser?.uid
                    val userMap = mapOf("email" to email)

                    if (uid != null) {
                        firebaseDatabase.getReference("users").child(uid).setValue(userMap)
                            .addOnSuccessListener {
                                Toast.makeText(this, "User registered!", Toast.LENGTH_SHORT).show()
                                startActivity(Intent(this, UsernameActivity::class.java))
                                finish()
                            }
                            .addOnFailureListener { e ->
                                Log.e("Firebase", "Failed to save user", e)
                                Toast.makeText(this, "Failed to save user", Toast.LENGTH_SHORT).show()
                            }
                    }
                } else {
                    val error = task.exception?.message
                    Log.e("FirebaseAuth", "Registration error: $error")
                    Toast.makeText(this, "Registration failed: $error", Toast.LENGTH_LONG).show()
                }
            }
    }
}

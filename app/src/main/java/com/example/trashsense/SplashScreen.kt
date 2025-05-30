package com.example.trashsense

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.trashsense.Question.Question1
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class SplashScreen : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_splash_screen)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        lifecycleScope.launch {
            delay(4000)

            val user = auth.currentUser

            if (user != null) {
                try {
                    val userDoc = db.collection("User")
                        .document(user.uid)
                        .get()
                        .await()

                    if (userDoc.exists()) {
                        val ecoActions = userDoc.get("ecoActions") as? List<*>
                        val ecoCount = ecoActions?.size ?: 0

                        if (ecoCount == 0) {
                            startActivity(Intent(this@SplashScreen, Question1::class.java))
                        } else {
                            startActivity(Intent(this@SplashScreen, HomeActivity::class.java))
                        }
                    } else {
                        startActivity(Intent(this@SplashScreen, Question1::class.java))
                    }

                } catch (e: Exception) {
                    Toast.makeText(this@SplashScreen, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this@SplashScreen, MainActivity::class.java))
                }
            } else {
                startActivity(Intent(this@SplashScreen, MainActivity::class.java))
            }

            finish()
        }
    }
}

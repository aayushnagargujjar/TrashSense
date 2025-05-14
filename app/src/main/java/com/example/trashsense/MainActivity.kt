package com.example.trashsense

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.widget.Toast
import android.widget.VideoView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var auth:FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
          auth  = FirebaseAuth.getInstance()
        var vv =findViewById<VideoView>(R.id.videoView)
        val videoPath = "android.resource://" + packageName + "/" + R.raw.trashsense_intro
        vv.setVideoURI(Uri.parse(videoPath))
        vv.start()
     if (auth.currentUser == null) {
         lifecycleScope.launch {
             delay(1000)
             val intent = Intent(this@MainActivity, WelcomeActivity::class.java)
             //  Toast.makeText(this@MainActivity, "Welcome back $email", Toast.LENGTH_SHORT).show()
             startActivity(intent)
             finish()
         }
     }
        else{
         lifecycleScope.launch {
             delay(1000)
             val intent = Intent(this@MainActivity, HomeActivity::class.java)
             Toast.makeText(this@MainActivity, "Welcome back ${auth.currentUser!!.email}", Toast.LENGTH_SHORT).show()
             startActivity(intent)
             finish()
         }
     }
    }
    }

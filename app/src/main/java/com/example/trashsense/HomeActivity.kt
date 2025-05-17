package com.example.trashsense

import HomeFragment
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.trashsense.AI_Eco_Dashboard.Ai_Eco_Dashboard


import com.example.trashsense.profile.Profile_Fragment
import com.example.trashsense.upload.UploadFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class HomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        var bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNavigationView.itemIconTintList = null
        bottomNavigationView.itemBackground = null

        var homeFragment =HomeFragment()
        var profilefrag =Profile_Fragment()
        var uploadfrag =UploadFragment()
        var EcoaiFragment =Ai_Eco_Dashboard()


        setCurrentFragment(homeFragment)


        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    setCurrentFragment(homeFragment)
                  //  val animation = AnimationUtils.loadAnimation(this, R.anim.bounce)
                    val iconView = bottomNavigationView.findViewById<View>(item.itemId)
                   // iconView.startAnimation(animation)
                    true
                }
                R.id.navigation_upload -> {
                    setCurrentFragment(uploadfrag)
                  //  val animation = AnimationUtils.loadAnimation(this, R.anim.bounce)
                    val iconView = bottomNavigationView.findViewById<View>(item.itemId)
                  //  iconView.startAnimation(animation)
                    true
                }
                R.id.navigation_profile -> {
                    setCurrentFragment(profilefrag)
                   // val animation = AnimationUtils.loadAnimation(this, R.anim.bounce)
                    val iconView = bottomNavigationView.findViewById<View>(item.itemId)
                  //  iconView.startAnimation(animation)
                    true
                }
                R.id.navigation_ECo_ai-> {
                    setCurrentFragment(EcoaiFragment)
                    true
                }

                else -> false
            }
        }
    }
   private fun setCurrentFragment(fragment: Fragment) =
       supportFragmentManager.beginTransaction().apply {
           replace(R.id.flFragment,fragment)
           commit()
       }


}
package com.example.trashsense.profile

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.trashsense.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class Profile_Fragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile_, container, false)

        val pfImageView = view.findViewById<ImageView>(R.id.profile_pic)
        val pfUsername = view.findViewById<TextView>(R.id.pf_username)
        val uid = auth.currentUser?.uid

        if (uid != null) {
            db.collection("User").document(uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val username = document.getString("Username") ?: "Unknown"
                        val imageUrl = document.getString("url") ?: ""

                        pfUsername.text = username

                        if (imageUrl.isNotEmpty()) {
                            Log.d("ProfileFragment", "Loading image: $imageUrl")
                            Glide.with(requireContext())
                                .load(imageUrl)
                                .fitCenter()
                                .circleCrop()
                                .placeholder(R.drawable.upload_24px)
                                .error(R.drawable.account_circle_24px)
                                .into(pfImageView)
                        } else {
                            Log.e("ProfileFragment", "Image URL is empty or null.")
                        }
                    } else {
                        Log.e("ProfileFragment", "Document does not exist.")
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("Firestore", "Error getting document", exception)
                }
        } else {
            Log.e("ProfileFragment", "UID is null, user not authenticated.")
        }

        return view
    }
}

package com.example.trashsense.home

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.trashsense.R
import com.example.trashsense.profile.Profile_Fragment
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class HomeFragment : Fragment() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var postData: ArrayList<Post_Data>
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: PostAdapter
    private lateinit var ecoTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        postData = ArrayList()
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        recyclerView = view.findViewById(R.id.homefragment_rview)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        val animationController = AnimationUtils.loadLayoutAnimation(requireContext(), R.anim.layout_fall_down)
        recyclerView.layoutAnimation = animationController

        var pfbtn =view.findViewById<ImageButton>(R.id.home_pf_btn)
        pfbtn.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.flFragment,Profile_Fragment())
                .addToBackStack(null)
                .commit()
        }

        val currentUserUid = auth.currentUser?.uid
        if (currentUserUid == null) {
            Toast.makeText(requireContext(), "User not logged in. Please log in.", Toast.LENGTH_LONG).show()
            return view
        }

        adapter = PostAdapter(postData, currentUserUid)
        recyclerView.adapter = adapter

        ecoTextView = view.findViewById(R.id.topEcoActionsText)

        loadPosts()
        loadEcoText()

        return view
    }

    @SuppressLint("SetTextI18n")
    private fun loadEcoText() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            ecoTextView.text = "Top Eco Actions for You:\n" +
                    "- Install LED bulbs — Save energy at home\n" +
                    "- Compost kitchen waste — Great for gardeners\n" +
                    "- Switch to bamboo toothbrush — Zero-waste starter"
            return
        }

        db.collection("User").document(userId).get()
            .addOnSuccessListener { document ->
                val ecoActionsList = document.get("ecoActions") as? List<String>
                ecoTextView.text = if (!ecoActionsList.isNullOrEmpty()) {
                    ecoActionsList.joinToString("\n")
                } else {
                    "Top Eco Actions for You:\n" +
                            "- Install LED bulbs — Save energy at home\n" +
                            "- Compost kitchen waste — Great for gardeners\n" +
                            "- Switch to bamboo toothbrush — Zero-waste starter"
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Failed to load eco actions: ${e.message}", Toast.LENGTH_SHORT).show()
                ecoTextView.text = "Top Eco Actions for You:\n" +
                        "- Install LED bulbs — Save energy at home\n" +
                        "- Compost kitchen waste — Great for gardeners\n" +
                        "- Switch to bamboo toothbrush — Zero-waste starter"
            }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun loadPosts() {
        db.collection("Posts")
            .document("Data")
            .collection("Aayush")
            .get()
            .addOnSuccessListener { documents ->
                postData.clear()
                for (document in documents) {
                    val pfUrl = document.getString("Profile_url") ?: ""
                    val text = document.getString("text") ?: ""
                    val imageUrl = document.getString("url") ?: ""
                    val username = document.getString("Username") ?: "Gurjar"
                    val like = document.getDouble("like")?.toInt() ?: 0
                    val likedByList = document.get("likedBy") as? ArrayList<String> ?: arrayListOf()

                    val post = Post_Data(
                        pf_url = pfUrl,
                        text = text,
                        image_url = imageUrl,
                        Username = username,
                        like = like,
                        postId = document.id,
                        likedBy = likedByList
                    )
                    postData.add(post)
                }
                adapter.notifyDataSetChanged()
                recyclerView.scheduleLayoutAnimation()
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Failed to load posts: ${e.message} 😔", Toast.LENGTH_SHORT).show()
            }
    }
}

package com.example.trashsense.Leaderboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.ImageView
import androidx.core.view.get
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.trashsense.R
import com.google.firebase.firestore.FirebaseFirestore

class Leaderboard : Fragment() {

    private lateinit var co2Container: LinearLayout
    private lateinit var waterContainer: LinearLayout
    private lateinit var actionContainer: LinearLayout

    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_leaderboard, container, false)
        co2Container = view.findViewById(R.id.co2_container)
        waterContainer = view.findViewById(R.id.water_container)
        actionContainer = view.findViewById(R.id.action_container)
        fetchLeaderboardData()

        return view
    }

    private fun fetchLeaderboardData() {
        db.collection("User").get().addOnSuccessListener { snapshot ->
            val users = snapshot.documents.mapNotNull { doc ->
                val name = doc.getString("Username") ?: return@mapNotNull null
                val avatarUrl = doc.getString("url")
                val co2 = doc.getDouble("total_co2_savings") ?: 0.0
                val water = doc.getDouble("total_water_savings") ?: 0.0
                val actions = doc.getDouble("Eco_Activity") ?: 0.0

                LeaderboardUser(name, avatarUrl, co2, water, actions)
            }

            populateLeaderboard(co2Container, users.sortedBy { it.totalCo2 }.take(3), "gm")
            populateLeaderboard(waterContainer, users.sortedByDescending { it.totalWater }.take(3), "ml")
            populateLeaderboard(actionContainer, users.sortedByDescending { it.totalActions }.take(3), "actions")
        }
    }

    private fun populateLeaderboard(container: LinearLayout, users: List<LeaderboardUser>, unit: String) {
        container.removeAllViews()
        val inflater = LayoutInflater.from(requireContext())

        users.forEachIndexed { index, user ->
            val itemView = inflater.inflate(R.layout.item_leaderboard_user, container, false)
            val rankText = itemView.findViewById<TextView>(R.id.user_rank)
            val avatarView = itemView.findViewById<ImageView>(R.id.user_avatar)
            val nameText = itemView.findViewById<TextView>(R.id.user_name)
            val scoreText = itemView.findViewById<TextView>(R.id.user_score)


            rankText.text = when (index) {
                0 -> "🥇"
                1 -> "🥈"
                2 -> "🥉"
                else -> (index + 1).toString()
            }

            nameText.text = user.name
            scoreText.text = when (unit) {
                "gm" -> "${user.totalCo2.toInt()} gm"
                "ml" -> "${user.totalWater.toInt()} ml"
                else -> "${user.totalActions.toInt()} actions"
            }

            // 👤 Avatar
            if (!user.avatarUrl.isNullOrEmpty()) {
                Glide.with(this)
                    .load(user.avatarUrl)
                    .error(R.drawable.account_circle_24px)
                    .circleCrop()
                    .into(avatarView)
            }

            // 💫 Animation on entry
            itemView.alpha = 0f
            itemView.translationY = 50f
            itemView.animate()
                .alpha(1f)
                .translationY(0f)
                .setStartDelay(index * 100L)
                .setDuration(600)
                .start()

            container.addView(itemView)
        }
    }


    data class LeaderboardUser(
        val name: String,
        val avatarUrl: String?,
        val totalCo2: Double,
        val totalWater: Double,
        val totalActions: Double
    )
}

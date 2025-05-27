package com.example.trashsense.home

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri // Import for handling image URIs
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.trashsense.R
import com.google.firebase.firestore.FieldValue // Import for atomic array updates
import com.google.firebase.firestore.FirebaseFirestore

class PostAdapter(
    private val postList: MutableList<Post_Data>, // Changed to MutableList if you plan to modify it directly
    private val currentUserUid: String // Pass the current user's UID to the adapter
) : RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    // No longer need the HashSet for tracking likes if we're relying on Firestore's likedBy array
    // private val likedPosts = HashSet<String>()

    class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val pfImage: ImageView = itemView.findViewById(R.id.Home_post_pf_pic)
        val username: TextView = itemView.findViewById(R.id.Home_post_username)
        val text: TextView = itemView.findViewById(R.id.Home_post_text)
        val postImage: ImageView = itemView.findViewById(R.id.imageView4)
        val likebtn: ImageButton = itemView.findViewById(R.id.Home_post_like_button)
        val likecount: TextView = itemView.findViewById(R.id.Home_post_like_count)
        val sharebtn: ImageButton = itemView.findViewById(R.id.Home_post_share_button)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = postList[position] // Get the current post data

        // Set the text fields
        holder.username.text = post.Username
        holder.text.text = post.text
        holder.likecount.text = post.like.toString()

        // Set initial like icon based on whether the current user has liked this post
        if (post.likedBy.contains(currentUserUid)) {
            holder.likebtn.setImageResource(R.drawable.ic_like) // Liked icon (e.g., green, filled heart) 👍
        } else {
            holder.likebtn.setImageResource(R.drawable.ic_unlike) // Unliked icon (e.g., grey, outline heart) 👎
        }

        // Handle Like/Unlike button click
        holder.likebtn.setOnClickListener {
            val postId = post.postId // Get the ID of the post

            // Check if the current user has already liked this post
            if (post.likedBy.contains(currentUserUid)) {
                // User HAS liked it, so now they are UNLIKING it
                Toast.makeText(holder.itemView.context, "Unliked! 💔", Toast.LENGTH_SHORT).show()

                // Update UI immediately (optimistic update)
                val newLike = post.like - 1
                holder.likecount.text = newLike.toString()
                holder.likebtn.setImageResource(R.drawable.ic_unlike)
                post.like = newLike // Update local data
                post.likedBy.remove(currentUserUid) // Remove UID from local list

                // Update Firestore: Decrement like count and remove user's UID
                FirebaseFirestore.getInstance()
                    .collection("Posts")
                    .document("Data")
                    .collection("Aayush") // IMPORTANT: Make this dynamic if "Aayush" is a user ID
                    .document(postId)
                    .update(
                        "like", FieldValue.increment(-1), // Decrement by 1
                        "likedBy", FieldValue.arrayRemove(currentUserUid) // Remove user's UID
                    )
                    .addOnSuccessListener {
                        // Success! No extra Toast needed as we already showed "Unliked!"
                    }
                    .addOnFailureListener { e ->
                        val originalLike = post.like + 1
                        holder.likecount.text = originalLike.toString()
                        holder.likebtn.setImageResource(R.drawable.ic_like)
                        post.like = originalLike // Revert local data
                        post.likedBy.add(currentUserUid) // Add UID back to local list
                        Toast.makeText(holder.itemView.context, "Failed to unlike: ${e.message} 😟", Toast.LENGTH_LONG).show()
                    }
            } else {
                // User has NOT liked it, so now they are LIKING it
                Toast.makeText(holder.itemView.context, "Liked! ❤️", Toast.LENGTH_SHORT).show()


                val newLike = post.like + 1
                holder.likecount.text = newLike.toString()
                holder.likebtn.setImageResource(R.drawable.ic_like)
                post.like = newLike // Update local data
                post.likedBy.add(currentUserUid) // Add UID to local list

                // Update Firestore: Increment like count and add user's UID
                FirebaseFirestore.getInstance()
                    .collection("Posts")
                    .document("Data")
                    .collection("Aayush") // IMPORTANT: Make this dynamic if "Aayush" is a user ID
                    .document(postId)
                    .update(
                        "like", FieldValue.increment(1), // Increment by 1
                        "likedBy", FieldValue.arrayUnion(currentUserUid) // Add user's UID
                    )
                    .addOnSuccessListener {
                        // Success! No extra Toast needed as we already showed "Liked!"
                    }
                    .addOnFailureListener { e ->
                        // Revert UI changes if Firestore update fails
                        val originalLike = post.like - 1
                        holder.likecount.text = originalLike.toString()
                        holder.likebtn.setImageResource(R.drawable.ic_unlike)
                        post.like = originalLike // Revert local data
                        post.likedBy.remove(currentUserUid) // Remove UID from local list
                        Toast.makeText(holder.itemView.context, "Failed to like: ${e.message} 😞", Toast.LENGTH_LONG).show()
                    }
            }
        }

        // Handle Share button click
        holder.sharebtn.setOnClickListener {
            val context = holder.itemView.context
            val shareText = "🌿 ${post.Username}'s Eco Tip from TrashSense:\n\n${post.text}\n\n#TrashSense #EcoFriendly"

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain" // Default to text/plain
                putExtra(Intent.EXTRA_SUBJECT, "Eco Tip from TrashSense")
                putExtra(Intent.EXTRA_TEXT, shareText)

            }

            val chooser = Intent.createChooser(shareIntent, "Share this Eco Tip via")
            // Check if there's any app that can handle the intent before starting
            if (shareIntent.resolveActivity(context.packageManager) != null) {
                context.startActivity(chooser)
            } else {
                Toast.makeText(context, "No app found to share content.", Toast.LENGTH_SHORT).show()
            }
        }

        // Load profile picture
        Glide.with(holder.itemView.context)
            .load(post.pf_url)
            .placeholder(R.drawable.img) // A placeholder image if pf_url is empty or loading
            .error(R.drawable.img) // An error image if loading fails
            .circleCrop() // Make the profile picture circular
            .into(holder.pfImage)

        // Load post image
        Glide.with(holder.itemView.context)
            .load(post.image_url)
            .placeholder(R.drawable.img) // A placeholder image if image_url is empty or loading
            .error(R.drawable.img) // An error image if loading fails
            .centerCrop() // Crop the image to fit the ImageView
            .into(holder.postImage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        // Inflate the layout for a single post item
        val view = LayoutInflater.from(parent.context).inflate(R.layout.post_rview, parent, false)
        return PostViewHolder(view)
    }

    override fun getItemCount(): Int = postList.size
}
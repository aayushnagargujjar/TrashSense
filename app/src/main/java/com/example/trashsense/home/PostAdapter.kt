package com.example.trashsense.home

import android.annotation.SuppressLint
import android.content.Intent
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
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class PostAdapter(
    private val postList: MutableList<Post_Data>,
    private val currentUserUid: String
) : RecyclerView.Adapter<PostAdapter.PostViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {

        val view = LayoutInflater.from(parent.context).inflate(R.layout.post_rview, parent, false)
        return PostViewHolder(view)
    }

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
        val post = postList[position]


        holder.username.text = post.Username
        holder.text.text = post.text
        holder.likecount.text = post.like.toString()


        if (post.likedBy.contains(currentUserUid)) {
            holder.likebtn.setImageResource(R.drawable.ic_like)
        } else {
            holder.likebtn.setImageResource(R.drawable.ic_unlike)
        }

        holder.likebtn.setOnClickListener {
            val postId = post.postId
            if (post.likedBy.contains(currentUserUid)) {
                Toast.makeText(holder.itemView.context, "Unliked! 💔", Toast.LENGTH_SHORT).show()
                val newLike = post.like - 1
                holder.likecount.text = newLike.toString()
                holder.likebtn.setImageResource(R.drawable.ic_unlike)
                post.like = newLike
                post.likedBy.remove(currentUserUid)

                FirebaseFirestore.getInstance()
                    .collection("Posts")
                    .document("Data")
                    .collection("Aayush")
                    .document(postId)
                    .update(
                        "like", FieldValue.increment(-1),
                        "likedBy", FieldValue.arrayRemove(currentUserUid)
                    )
                    .addOnSuccessListener {

                    }
                    .addOnFailureListener { e ->
                        val originalLike = post.like + 1
                        holder.likecount.text = originalLike.toString()
                        holder.likebtn.setImageResource(R.drawable.ic_like)
                        post.like = originalLike
                        post.likedBy.add(currentUserUid)
                        Toast.makeText(holder.itemView.context, "Failed to unlike: ${e.message} 😟", Toast.LENGTH_LONG).show()
                    }
            } else {

                Toast.makeText(holder.itemView.context, "Liked! ❤️", Toast.LENGTH_SHORT).show()


                val newLike = post.like + 1
                holder.likecount.text = newLike.toString()
                holder.likebtn.setImageResource(R.drawable.ic_like)
                post.like = newLike
                post.likedBy.add(currentUserUid)
                FirebaseFirestore.getInstance()
                    .collection("Posts")
                    .document("Data")
                    .collection("Aayush")
                    .document(postId)
                    .update(
                        "like", FieldValue.increment(1),
                        "likedBy", FieldValue.arrayUnion(currentUserUid)
                    )
                    .addOnSuccessListener {

                    }
                    .addOnFailureListener { e ->
                        val originalLike = post.like - 1
                        holder.likecount.text = originalLike.toString()
                        holder.likebtn.setImageResource(R.drawable.ic_unlike)
                        post.like = originalLike
                        post.likedBy.remove(currentUserUid)
                        Toast.makeText(holder.itemView.context, "Failed to like: ${e.message} 😞", Toast.LENGTH_LONG).show()
                    }
            }
        }

        holder.sharebtn.setOnClickListener {
            val context = holder.itemView.context
            val shareText = "🌿 ${post.Username}'s Eco Tip from TrashSense:\n\n${post.text}\n\n#TrashSense #EcoFriendly"

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, "Eco Tip from TrashSense")
                putExtra(Intent.EXTRA_TEXT, shareText)

            }

            val chooser = Intent.createChooser(shareIntent, "Share this Eco Tip via")
            if (shareIntent.resolveActivity(context.packageManager) != null) {
                context.startActivity(chooser)
            } else {
                Toast.makeText(context, "No app found to share content.", Toast.LENGTH_SHORT).show()
            }
        }


        Glide.with(holder.itemView.context)
            .load(post.pf_url)
            .placeholder(R.drawable.account_circle_24px)
            .error(R.drawable.account_circle_24px)
            .circleCrop()
            .into(holder.pfImage)


        Glide.with(holder.itemView.context)
            .load(post.image_url)
            .placeholder(R.drawable.img)
            .error(R.drawable.img)
            .centerCrop()
            .into(holder.postImage)
    }


    override fun getItemCount(): Int = postList.size
}
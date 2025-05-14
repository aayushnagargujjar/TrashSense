package com.example.trashsense

import com.example.trashsense.home.Post_Data



import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.trashsense.R

class PostAdapter(private val postList: List<Post_Data>) :
    RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val pfImage: ImageView = itemView.findViewById(R.id.Home_post_pf_pic)
        val username: TextView = itemView.findViewById(R.id.Home_post_username)
        val text: TextView = itemView.findViewById(R.id.Home_post_text)
        val postImage: ImageView = itemView.findViewById(R.id.imageView4)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.post_rview, parent, false) // make sure your XML file is named item_post.xml
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = postList[position]
        holder.username.text = post.Username
        holder.text.text = post.text

        Glide.with(holder.itemView.context)
            .load(post.pf_url)
            .placeholder(R.drawable.img)
            .error(R.drawable.img)
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

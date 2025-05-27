package com.example.trashsense.home

import com.google.firebase.firestore.DocumentId // Import this for automatic ID mapping

data class Post_Data(
    // @DocumentId automatically assigns the Firestore document ID to this field
    @DocumentId
    var pf_url: String = "", // Profile picture URL
    var text: String = "", // Post main text content
    var image_url: String = "", // Post image URL
    var Username: String = "", // Username of the poster
    var like: Int = 0, // Number of likes for the post
    var postId: String = "", // Unique ID for each post in Firestore
    val likedBy: ArrayList<String> = arrayListOf()
)
package com.example.trashsense.home

import com.google.firebase.firestore.DocumentId

data class Post_Data(

    @DocumentId
    var pf_url: String = "",
    var text: String = "",
    var image_url: String = "",
    var Username: String = "",
    var like: Int = 0,
    var postId: String = "",
    val likedBy: ArrayList<String> = arrayListOf()
)
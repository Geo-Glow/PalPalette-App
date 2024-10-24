package com.example.geoglow.data.model

data class ColorMultiPost(
    val fromFriendId: String,
    val toFriendIds: List<String>,
    val colors: List<String>,
    val imageData: String,
)

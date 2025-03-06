package com.app.geoglow.data.model

data class ColorMultiPost(
    val fromFriendId: String,
    val toFriendIds: List<String>,
    val colors: List<String>,
    val imageData: String,
)

data class Timeout(
    val start: String,
    val end: String
)
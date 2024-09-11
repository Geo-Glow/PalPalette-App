package com.example.geoglow.data.model

data class Friend(
    val friendId: String,
    val tileIds: List<Int>,
    val groupId: String,
    val name: String,
    val lastPing: String,
    val queue: List<String>
)

data class ColorRequest(val fromFriendId: String, val colors: List<String>)
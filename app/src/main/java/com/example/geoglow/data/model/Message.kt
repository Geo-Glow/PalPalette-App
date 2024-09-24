package com.example.geoglow.data.model

import com.google.gson.annotations.SerializedName
import java.util.Date

data class Message(
    @SerializedName("_id") val id: String,
    @SerializedName("colors") val colors: List<String>,
    @SerializedName("toFriendId") val toFriendId: String,
    @SerializedName("fromFriendId") val fromFriendId: String,
    @SerializedName("timestamp") val timestamp: Date
)

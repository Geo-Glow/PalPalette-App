package com.example.geoglow.network.api

import com.example.geoglow.data.model.ColorMultiPost
import com.example.geoglow.data.model.ColorRequest
import com.example.geoglow.data.model.Friend
import com.example.geoglow.data.model.Message
import retrofit2.Call
import retrofit2.http.*

interface ApiService {

    @GET("friends")
    fun getAllFriends(@Query("groupId") groupId: String?): Call<List<Friend>>

    @GET("friends/{friendId}")
    fun getFriendById(@Path("friendId") friendId: String): Call<Friend>

    @POST("friends/colors")
    fun sendColors(
        @Body colorRequest: ColorMultiPost
    ): Call<Void>

    @POST("friends/{friendId}/colors")
    fun sendColors(
        @Path("friendId") friendId: String,
        @Body colorRequest: ColorRequest,
        @Query("shouldSaveMessage") shouldSaveMessage: Boolean
    ): Call<Void>

    @GET("messages")
    fun getMessages(
        @Query("toFriendId") toFriendId: String?,
        @Query("fromFriendId") fromFriendId: String?,
        @Query("startTime") startTime: Long?,
        @Query("endTime") endTime: Long?
    ): Call<List<Message>>
}
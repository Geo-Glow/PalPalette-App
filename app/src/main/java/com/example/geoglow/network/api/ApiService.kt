package com.example.geoglow.network.api

import com.example.geoglow.data.model.ColorRequest
import com.example.geoglow.data.model.Friend
import retrofit2.Call
import retrofit2.http.*

interface ApiService {

    @GET("friends")
    fun getAllFriends(@Query("groupId") groupId: String?): Call<List<Friend>>

    @POST("friends/{friendId}/colors")
    fun sendColors(@Path("friendId") friendId: String, @Body colorRequest: ColorRequest): Call<Void>
}
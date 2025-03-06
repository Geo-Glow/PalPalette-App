package com.app.geoglow.network.api

import com.app.geoglow.data.model.ColorMultiPost
import com.app.geoglow.data.model.ColorRequest
import com.app.geoglow.data.model.ColorResponse
import com.app.geoglow.data.model.Friend
import com.app.geoglow.data.model.Message
import com.app.geoglow.data.model.Timeout
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.*

interface ApiService {

    @GET("friends")
    fun getAllFriends(@Query("groupId") groupId: String?): Call<List<Friend>>

    @Multipart
    @POST("colors/upload")
    fun uploadImage(@Part image: MultipartBody.Part): Call<ColorResponse>

    @GET("friends/{friendId}")
    fun getFriendById(@Path("friendId") friendId: String): Call<Friend>

    @POST("friends/colors")
    fun sendColors(
        @Body colorRequest: ColorMultiPost
    ): Call<Void>

    @POST("friends/{friendId}/timeout")
    fun sendTimeoutTimes(
        @Path("friendId") friendId: String,
        @Body timeout: Timeout
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
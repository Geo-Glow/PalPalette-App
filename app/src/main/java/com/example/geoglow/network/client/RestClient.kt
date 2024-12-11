package com.example.geoglow.network.client

import android.content.Context
import com.example.geoglow.network.api.ApiService
import com.example.geoglow.SendColorsResult
import com.example.geoglow.data.model.ColorMultiPost
import com.example.geoglow.data.model.ColorRequest
import com.example.geoglow.data.model.Friend
import com.example.geoglow.data.model.Message
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RestClient(private val context: Context) {

    companion object {
        private const val TAG = "RESTClient"
        private const val BASE_URL = "http://139.6.56.197"
    }

    private val apiService: ApiService

    init {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(GsonBuilder().create()))
            .build()

        apiService = retrofit.create(ApiService::class.java)
    }

    fun getAllFriends(groupId: String?, onResult: (List<Friend>?, Throwable?) -> Unit) {
        apiService.getAllFriends(groupId).enqueue(object : Callback<List<Friend>> {
            override fun onResponse(call: Call<List<Friend>>, response: Response<List<Friend>>) {
                if (response.isSuccessful) {
                    onResult(response.body(), null)
                } else {
                    onResult(null, Throwable(response.errorBody()?.string()))
                }
            }

            override fun onFailure(call: Call<List<Friend>>, t: Throwable) {
                onResult(null, t)
            }
        })
    }

    fun getFriendById(friendId: String, onResult: (Friend?, Throwable?) -> Unit) {
        apiService.getFriendById(friendId).enqueue(object : Callback<Friend> {
            override fun onResponse(call: Call<Friend>, response: Response<Friend>) {
                if (response.isSuccessful) {
                    onResult(response.body(), null)
                } else {
                    onResult(null, Throwable(response.errorBody()?.string()))
                }
            }

            override fun onFailure(call: Call<Friend>, t: Throwable) {
                onResult(null, t)
            }
        })
    }

    fun sendColors(toFriendId: String, fromFriendId: String, colors: List<String>, shouldSaveMessage: Boolean, onResult: (SendColorsResult, Throwable?) -> Unit) {
        val colorRequest = ColorRequest(fromFriendId, colors)
        apiService.sendColors(toFriendId, colorRequest, shouldSaveMessage).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                when (response.code()) {
                    200 -> onResult(SendColorsResult.SUCCESS, null)
                    202 -> onResult(SendColorsResult.ACCEPTED, null)
                    404 -> onResult(
                        SendColorsResult.FRIEND_NOT_FOUND,
                        Throwable("Friend not found")
                    )
                    500 -> onResult(
                        SendColorsResult.SERVER_ERROR,
                        Throwable("Internal server error")
                    )
                    else -> onResult(
                        SendColorsResult.UNKNOWN_ERROR,
                        Throwable("Unknown error: ${response.code()}")
                    )
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                onResult(SendColorsResult.UNKNOWN_ERROR, t)
            }
        })
    }

    fun sendColors(fromFriendId: String, toFriendIds: List<String>, colors: List<String>, imageData: String, onResult: (SendColorsResult, Throwable?) -> Unit) {
        val colorRequest = ColorMultiPost(fromFriendId, toFriendIds, colors, imageData)
        apiService.sendColors(colorRequest).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                when (response.code()) {
                    200 -> onResult(SendColorsResult.SUCCESS, null)
                    207 -> onResult(SendColorsResult.PARTIAL_SUCCESS, null)
                    400 -> onResult(
                        SendColorsResult.BAD_REQUEST,
                        Throwable("Invalid request data")
                    )
                    404 -> onResult(
                        SendColorsResult.FRIEND_NOT_FOUND,
                        Throwable("One or more friends not found")
                    )
                    500 -> onResult(
                        SendColorsResult.SERVER_ERROR,
                        Throwable("Internal server error")
                    )
                    else -> onResult(
                        SendColorsResult.UNKNOWN_ERROR,
                        Throwable("Unknown error: ${response.code()}")
                    )
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                onResult(SendColorsResult.UNKNOWN_ERROR, t)
            }
        })
    }

    fun getMessages(toFriendId: String?, fromFriendId: String?, onResult: (List<Message>?, Throwable?) -> Unit) {
        apiService.getMessages(toFriendId, fromFriendId, null, null).enqueue(object: Callback<List<Message>> {
            override fun onResponse(call: Call<List<Message>>, response: Response<List<Message>>) {
                if (response.isSuccessful) {
                    onResult(response.body(), null)
                } else {
                    onResult(null, Throwable(response.errorBody()?.string()))
                }
            }

            override fun onFailure(call: Call<List<Message>>, t: Throwable) {
                onResult(null, t)
            }
        })
    }

    fun getMessagesWithTimeFrame(toFriendId: String?, fromFriendId: String?, startTime: Long?, endTime: Long?, onResult: (List<Message>?, Throwable?) -> Unit) {
        apiService.getMessages(toFriendId, fromFriendId, startTime, endTime).enqueue(object: Callback<List<Message>> {
            override fun onResponse(call: Call<List<Message>>, response: Response<List<Message>>) {
                if (response.isSuccessful) {
                    onResult(response.body(), null)
                } else {
                    onResult(null, Throwable(response.errorBody()?.string()))
                }
            }

            override fun onFailure(call: Call<List<Message>>, t: Throwable) {
                onResult(null, t)
            }
        })
    }
}
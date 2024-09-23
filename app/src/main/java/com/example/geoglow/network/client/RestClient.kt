package com.example.geoglow.network.client

import android.content.Context
import android.util.Log
import com.example.geoglow.network.api.ApiService
import com.example.geoglow.SendColorsResult
import com.example.geoglow.data.model.ColorRequest
import com.example.geoglow.data.model.Friend
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

    fun sendColors(toFriendId: String, fromFriendId: String, colors: List<String>, onResult: (SendColorsResult, Throwable?) -> Unit) {
        val colorRequest = ColorRequest(fromFriendId, colors)
        apiService.sendColors(toFriendId, colorRequest).enqueue(object : Callback<Void> {
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
}
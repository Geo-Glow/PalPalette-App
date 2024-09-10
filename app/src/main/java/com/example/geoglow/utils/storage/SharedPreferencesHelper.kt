package com.example.geoglow.utils.storage

import android.content.Context
import android.content.SharedPreferences
import com.example.geoglow.data.model.Friend
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object SharedPreferencesHelper {
    private const val PREFS_NAME = "app_preferences"
    private const val KEY_FRIEND_LIST = "friends_list"
    private val gson = Gson()

    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    /**
     * Save the list of friends to shared preferences.
     */
    fun setFriendList(context: Context, friends: List<Friend>) {
        val friendsJson = gson.toJson(friends)
        getPreferences(context).edit().putString(KEY_FRIEND_LIST, friendsJson).apply()
    }

    /**
     * Retrieve the list of friends from shared preferences.
     */
    fun getFriendList(context: Context): List<Friend> {
        val friendsJson = getPreferences(context).getString(KEY_FRIEND_LIST, null)
        return friendsJson?.let {
            val type = object : TypeToken<List<Friend>>() {}.type
            gson.fromJson(it, type)
        } ?: emptyList()
    }

    /**
     * Clear all saved preferences.
     */
    fun resetPreferences(context: Context) {
        getPreferences(context).edit().clear().apply()
    }
}
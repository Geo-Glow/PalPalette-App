package com.app.geoglow.utils.storage

import android.content.Context
import android.content.SharedPreferences
import com.app.geoglow.data.model.Friend
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object SharedPreferencesHelper {
    private const val PREFS_NAME = "app_preferences"
    private const val KEY_FRIEND_LIST = "friends_list"
    private const val KEY_SEEN_MESSAGES = "seen_messages"
    private const val KEY_UNSEEN_ONLY = "unseen_only"
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

    fun addSeenMessages(context: Context, messageId: String) {
        val seenMessages = getSeenMessages(context).toMutableSet()
        seenMessages.add(messageId)
        saveSeenMessages(context, seenMessages)
    }

    private fun saveSeenMessages(context: Context, messages: Set<String>) {
        val messagesJson = gson.toJson(messages)
        getPreferences(context).edit().putString(KEY_SEEN_MESSAGES, messagesJson).apply()
    }

    fun getSeenMessages(context: Context): Set<String> {
        val messagesJson = getPreferences(context).getString(KEY_SEEN_MESSAGES, null)
        return messagesJson?.let {
            val type = object : TypeToken<Set<String>>() {}.type
            gson.fromJson(it, type)
        } ?: emptySet()
    }

    fun setUnseenOnlyPreference(context: Context, value: Boolean) {
        getPreferences(context).edit().putBoolean(KEY_UNSEEN_ONLY, value).apply()
    }

    fun getUnseenOnlyPreference(context: Context): Boolean {
        return getPreferences(context).getBoolean(KEY_UNSEEN_ONLY, false)
    }

    /**
     * Clear all saved preferences.
     */
    fun resetPreferences(context: Context) {
        getPreferences(context).edit().clear().apply()
    }
}
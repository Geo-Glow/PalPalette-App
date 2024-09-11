package com.example.geoglow.utils.storage

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore("settings")

class DataStoreManager(private val context: Context) {
    companion object {
        val FRIEND_ID_KEY = stringPreferencesKey("FriendID")
    }

    val friendIDExists = context.dataStore.data.map { preferences ->
        preferences[FRIEND_ID_KEY]?.isNotEmpty() ?: false
    }

    val friendID = context.dataStore.data.map { preferences ->
        preferences[FRIEND_ID_KEY] ?: ""
    }

    suspend fun storeFriendID(friendID: String) {
        context.dataStore.edit { preferences ->
            preferences[FRIEND_ID_KEY] = friendID
        }
    }
}
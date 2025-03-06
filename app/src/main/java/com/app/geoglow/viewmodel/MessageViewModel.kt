package com.app.geoglow.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.app.geoglow.utils.storage.DataStoreManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class MessageViewModel(application: Application, dataStoreManager: DataStoreManager) :
    AndroidViewModel(application) {
    val groupId: StateFlow<String> =
        dataStoreManager.groupId.stateIn(viewModelScope, SharingStarted.Lazily, initialValue = "")
}
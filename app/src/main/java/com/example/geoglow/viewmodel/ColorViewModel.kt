package com.example.geoglow.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.palette.graphics.Palette
import com.example.geoglow.network.client.RestClient
import com.example.geoglow.data.model.Friend
import com.example.geoglow.utils.general.paletteToRgbList
import com.example.geoglow.utils.general.rotateImage
import com.example.geoglow.utils.storage.SharedPreferencesHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ColorViewModel(application: Application): AndroidViewModel(application) {

    data class ColorState(
        val imageBitmap: ImageBitmap? = null,
        val colorList: List<Array<Int>>? = null
    )

    private val maxExtractedColors = 16
    private val _colorState = MutableStateFlow(ColorState())
    val colorState = _colorState.asStateFlow()

    private val _showFriendSelectionPopup = MutableStateFlow(false)
    val showFriendSelectionPopup = _showFriendSelectionPopup.asStateFlow()

    private val _friendList = MutableStateFlow<List<Friend>>(emptyList())
    val friendList = _friendList.asStateFlow()

    fun setColorState(uri: Uri, rotate: Boolean = false) {
        viewModelScope.launch(Dispatchers.IO) {
            getApplication<Application>().applicationContext.contentResolver.openInputStream(uri)?.use { stream ->
                val bitmap: Bitmap = BitmapFactory.decodeStream(stream)
                val rotatedImg = rotateImage(bitmap)
                Log.d("Color", "${rotatedImg.byteCount}")
                val palette = Palette.Builder(bitmap)
                    .maximumColorCount(maxExtractedColors)
                    .generate()
                _colorState.update { currentState ->
                    currentState.copy(
                        imageBitmap = if (rotate) rotatedImg.asImageBitmap() else bitmap.asImageBitmap(),
                        colorList = paletteToRgbList(palette)
                    )
                }
            }
        }
    }

    fun resetColorState() {
        _colorState.update { ColorState() }
    }

    fun setShowFriendSelectionPopup(show: Boolean) {
        _showFriendSelectionPopup.value = show
    }

    fun refreshFriendList(restClient: RestClient) {
        viewModelScope.launch(Dispatchers.IO) {
            restClient.getAllFriends(null) { friends, error ->
                if (error != null) {
                    // Handle error
                } else {
                    friends?.let { fetchedFriends ->
                        _friendList.update { fetchedFriends }
                        SharedPreferencesHelper.setFriendList(getApplication(), fetchedFriends)
                    }
                }
            }
        }
    }
}
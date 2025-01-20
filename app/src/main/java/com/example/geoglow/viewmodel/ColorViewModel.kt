package com.example.geoglow.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.exifinterface.media.ExifInterface
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.palette.graphics.Palette
import com.example.geoglow.data.model.Friend
import com.example.geoglow.network.client.RestClient
import com.example.geoglow.utils.general.paletteToRgbList
import com.example.geoglow.utils.storage.DataStoreManager
import com.example.geoglow.utils.storage.SharedPreferencesHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException

class ColorViewModel(application: Application): AndroidViewModel(application) {

    data class ColorState(
        val imageBitmap: ImageBitmap? = null,
        val colorList: List<Array<Int>>? = null,
        val imageMetadataJson: String = "",
        val errorMessage: String? = null
    )

    private val maxExtractedColors = 16
    private val _colorState = MutableStateFlow(ColorState())
    val colorState = _colorState.asStateFlow()

    private val _showFriendSelectionPopup = MutableStateFlow(false)
    val showFriendSelectionPopup = _showFriendSelectionPopup.asStateFlow()

    private val _friendList = MutableStateFlow<List<Friend>>(emptyList())
    val friendList = _friendList.asStateFlow()

    private val dataStoreManager: DataStoreManager = DataStoreManager(getApplication<Application>().applicationContext)

    // Get Image rotation from exif tags
    private fun getExifOrientation(uri: Uri): Int {
        return try {
            val inputStream = getApplication<Application>().applicationContext.contentResolver.openInputStream(uri)
            inputStream?.use {
                val exifInterface = ExifInterface(it)
                exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
            } ?: ExifInterface.ORIENTATION_NORMAL
        } catch (e: IOException) {
            e.printStackTrace()
            ExifInterface.ORIENTATION_NORMAL
        }
    }

    fun clearErrorMessage() {
        _colorState.update { currentState ->
            currentState.copy(
                errorMessage = null
            )
        }
    }

    // Rotate bitmap based on exif orientation
    private fun rotateBitmap(bitmap: Bitmap, orientation: Int): Bitmap {
        val matrix = android.graphics.Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
        }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    fun setColorState(uri: Uri, imageMetadata: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                getApplication<Application>().applicationContext.contentResolver.openInputStream(uri)?.use { stream ->
                    val bitmap: Bitmap = BitmapFactory.decodeStream(stream)
                    val orientation = getExifOrientation(uri)
                    val correctlyOrientedBitmap = rotateBitmap(bitmap, orientation)

                    val palette = Palette.Builder(correctlyOrientedBitmap)
                        .maximumColorCount(maxExtractedColors)
                        .generate()
                    if (palette.swatches.isNotEmpty()) {
                        _colorState.update { currentState ->
                            currentState.copy(
                                imageBitmap = correctlyOrientedBitmap.asImageBitmap(),
                                colorList = paletteToRgbList(palette),
                                imageMetadataJson = imageMetadata
                            )
                        }
                    } else {
                        _colorState.update { currentState ->
                            currentState.copy(
                                errorMessage = "No colors found in the image."
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                _colorState.update { currentState ->
                    currentState.copy(
                        errorMessage = "Failed to process the image."
                    )
                }
                e.printStackTrace()
            }
        }
    }

    fun updateColorList(newList: List<Array<Int>>) {
        _colorState.update { currentState ->
            currentState.copy(
                colorList = newList
            )
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
            val groupId = withContext(Dispatchers.IO) {
                dataStoreManager.groupId.first()
            }

            Log.d("GroupID", groupId)

            restClient.getAllFriends(groupId) { friends, error ->
                if (error != null) {
                    // Handle error
                } else {
                    friends?.let { fetchedFriends ->
                        Log.d("Nick", fetchedFriends.toString())
                        _friendList.update { fetchedFriends }
                        SharedPreferencesHelper.setFriendList(getApplication(), fetchedFriends)
                    }
                }
            }
        }
    }
}
package com.example.geoglow

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Matrix
import android.os.Build
import android.util.Log
import androidx.palette.graphics.Palette
import java.io.File
import org.json.JSONArray
import org.json.JSONObject

@SuppressLint("SimpleDateFormat")
fun Context.createImageFile(): File {
    val imageFileName = "JPEG_"
    val image = File.createTempFile(
        imageFileName, /* prefix */
        ".jpg", /* suffix */
        externalCacheDir /* directory */
    )
    return image
}

fun transformListToJson(name: String, list: List<Array<Int>>): JSONObject {
    val jsonArray = JSONArray()

    for (array in list) {
        val jsonArrayElement = JSONArray()
        for (value in array) {
            jsonArrayElement.put(value)
        }
        jsonArray.put(jsonArrayElement)
    }

    val jsonObject = JSONObject()
    jsonObject.put(name, jsonArray)
    return jsonObject
}

fun paletteToRgbList(palette: Palette): List<Array<Int>> {
    // Sort swatches by population
    val swatches = palette.swatches
        .sortedByDescending { it.population }
        .toMutableList()
    // If vibrant swatch exist add it to the front of the swatches list
    if (palette.vibrantSwatch != null) {
        swatches.add(0, palette.vibrantSwatch!!)
    }
    // Convert and return each swatch to RGB array
    return swatches.map { swatch ->
        arrayOf(
            Color.red(swatch.rgb),
            Color.green(swatch.rgb),
            Color.blue(swatch.rgb)
        )
    }
}

fun resizeBitmap(bitmap: Bitmap, factor: Int = 5): Bitmap {
    val width = bitmap.width
    val height = bitmap.height
    val scaleWidth = width / factor
    val scaleHeight = height / factor
    return Bitmap.createScaledBitmap(bitmap, scaleWidth, scaleHeight, true)
}

fun resizeBitmap(bitmap: Bitmap, width: Int = 500, height: Int = 500): Bitmap {
    return Bitmap.createScaledBitmap(bitmap, width, height, true)
}

// Rotate image for certain manufacturers
fun rotateImage(bitmap: Bitmap): Bitmap {
    Log.i("util", "Build.MANUFACTURER: ${Build.MANUFACTURER}")

    if (Build.MANUFACTURER == "samsung") {
        val rotationDegrees =  90
        val matrix = Matrix().apply { postRotate(rotationDegrees.toFloat()) }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    } else {
        return bitmap
    }
}
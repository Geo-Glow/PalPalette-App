package com.example.geoglow.utils.general

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import androidx.palette.graphics.Palette
import java.io.File
import java.util.Locale
import java.util.*
import java.text.SimpleDateFormat

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

fun paletteToRgbList(palette: Palette): List<Array<Int>> {
    // Sort swatches by population
    val swatches = palette.swatches
        .sortedByDescending { it.population }
        .toMutableList()

    if (palette.lightVibrantSwatch != null) {
        swatches.remove(palette.lightVibrantSwatch)
        swatches.add(0, palette.lightVibrantSwatch!!)
    }

    if (palette.darkVibrantSwatch != null) {
        swatches.remove(palette.darkVibrantSwatch)
        swatches.add(0, palette.darkVibrantSwatch!!)
    }

    // If vibrant swatch exist add it to the front of the swatches list
    if (palette.vibrantSwatch != null) {
        swatches.remove(palette.vibrantSwatch)
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

fun formatTimestamp(timestamp: Date): String {
    val formatter = SimpleDateFormat("dd MMM yyyy • hh:mm a", Locale.getDefault())
    formatter.timeZone = TimeZone.getDefault()
    return formatter.format(timestamp)
}
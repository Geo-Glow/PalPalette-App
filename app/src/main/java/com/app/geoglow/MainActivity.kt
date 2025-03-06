package com.app.geoglow

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import com.app.geoglow.network.client.RestClient
import com.app.geoglow.ui.theme.GeoGlowTheme
import com.app.geoglow.ui.navigation.Navigation
import com.app.geoglow.viewmodel.ColorViewModel
import org.opencv.android.OpenCVLoader

class MainActivity : ComponentActivity() {

    private val viewModel by viewModels<ColorViewModel>()
    private lateinit var restClient: RestClient

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        restClient = RestClient(this)

        if (OpenCVLoader.initLocal()) {
            Log.i("OpenCV", "OpenCV loaded successfully");
        } else {
            Log.e("OpenCV", "OpenCV initialization failed!");
            (Toast.makeText(this, "OpenCV initialization failed!", Toast.LENGTH_LONG)).show();
            return;
        }

        setContent {
            GeoGlowTheme {
                Navigation(viewModel, restClient)
            }
        }
    }
}
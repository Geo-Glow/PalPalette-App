package com.example.geoglow

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import com.example.geoglow.network.client.RestClient
import com.example.geoglow.ui.theme.GeoGlowTheme
import com.example.geoglow.ui.navigation.Navigation
import com.example.geoglow.viewmodel.ColorViewModel

class MainActivity : ComponentActivity() {

    private val viewModel by viewModels<ColorViewModel>()
    private lateinit var restClient: RestClient

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        restClient = RestClient(this)

        setContent {
            GeoGlowTheme {
                Navigation(viewModel, restClient)
            }
        }
    }
}
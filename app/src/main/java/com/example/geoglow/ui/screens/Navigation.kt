package com.example.geoglow.ui.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.geoglow.ColorViewModel
import com.example.geoglow.RestClient

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun Navigation(viewModel: ColorViewModel, restClient: RestClient) {
    val navController = rememberNavController()

    LaunchedEffect(Unit) {
        navController.addOnDestinationChangedListener {_, destination, _ ->
            if (destination.route == Screen.MainScreen.route) {
                viewModel.refreshFriendList(restClient)
            }
        }
    }

    NavHost(navController = navController, startDestination = Screen.MainScreen.route) {
        composable(route = Screen.MainScreen.route) {
            MainScreen(navController, viewModel, restClient)
        }

        composable(route = Screen.ImageScreen.route) {
            ImageScreen(navController, viewModel, restClient)
        }
    }
}
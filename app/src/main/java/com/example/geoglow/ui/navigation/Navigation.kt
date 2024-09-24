package com.example.geoglow.ui.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.geoglow.data.model.Message
import com.example.geoglow.viewmodel.ColorViewModel
import com.example.geoglow.network.client.RestClient
import com.example.geoglow.ui.screen.MainScreen
import com.example.geoglow.ui.screen.ImageScreen
import com.example.geoglow.ui.screen.MessageScreen

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
            MainScreen(navController, viewModel)
        }

        composable(route = Screen.ImageScreen.route) {
            ImageScreen(navController, viewModel, restClient)
        }

        composable(
            route = "${Screen.MessageScreen.route}/{friendID}",
            arguments = listOf(navArgument("friendID") { type = NavType.StringType })
        ) { navBackStackEntry ->
            val friendID = navBackStackEntry.arguments?.getString("friendID").orEmpty()
            MessageScreen(navController, friendID)
        }
    }
}
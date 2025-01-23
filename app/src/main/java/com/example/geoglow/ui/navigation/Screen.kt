package com.example.geoglow.ui.navigation

sealed class Screen(val route: String) {
    object MainScreen : Screen("main_screen")
    object ImageScreen : Screen("image_screen")
    object MessageScreen : Screen("message_screen")
    object SettingsScreen : Screen("settings_screen")

    fun withArgs(vararg args: String): String {
        return buildString {
            append(route)
            args.forEach { arg ->
                append("?$arg={$arg}")
            }
        }
    }
}
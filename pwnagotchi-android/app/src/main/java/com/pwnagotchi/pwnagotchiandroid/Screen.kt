package com.pwnagotchi.pwnagotchiandroid

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Settings : Screen("settings")
    object Plugins : Screen("plugins")
}

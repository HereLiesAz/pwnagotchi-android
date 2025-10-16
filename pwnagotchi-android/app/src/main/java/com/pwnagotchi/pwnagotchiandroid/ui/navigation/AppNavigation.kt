package com.pwnagotchi.pwnagotchiandroid.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Home : Screen("home", "Home", Icons.Default.Home)
    object Plugins : Screen("plugins", "Plugins", Icons.Default.List)
    object Opwngrid : Screen("opwngrid", "oPwngrid", Icons.Default.Info)
    object Settings : Screen("settings", "Settings", Icons.Default.Settings)
}

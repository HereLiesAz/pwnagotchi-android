package com.pwnagotchi.pwnagotchiandroid.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import com.pwnagotchi.pwnagotchiandroid.R

sealed class Screen(
    val route: String,
    val title: Int,
    val icon: ImageVector
) {
    object Home : Screen("home", R.string.screen_home, Icons.Default.Home)
    object Plugins : Screen("plugins", R.string.screen_plugins, Icons.AutoMirrored.Filled.List)
    object Opwngrid : Screen("opwngrid", R.string.screen_opwngrid, Icons.AutoMirrored.Filled.List)
    object Settings : Screen("settings", R.string.screen_settings, Icons.Default.Settings)
}
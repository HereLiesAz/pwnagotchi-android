package com.pwnagotchi.pwnagotchiOnAndroid.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.graphics.vector.ImageVector
import com.pwnagotchi.pwnagotchiOnAndroid.R

/**
 * A sealed class representing the different screens in the application.
 *
 * Each object defines a unique route, a string resource for the title, and an icon.
 */
sealed class Screen(val route: String, @StringRes val title: Int, val icon: ImageVector) {
    object Home : Screen("home", R.string.screen_home, Icons.Default.Home)
    object Plugins : Screen("plugins", R.string.screen_plugins, Icons.Default.List)
    object Opwngrid : Screen("opwngrid", R.string.screen_opwngrid, Icons.Default.Star)
    object Settings : Screen("settings", R.string.screen_settings, Icons.Default.Settings)
}

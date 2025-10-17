package com.hereliesaz.pwnagotchiOnAndroid.ui

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.hereliesaz.pwnagotchiOnAndroid.PwnagotchiUiState
import com.hereliesaz.pwnagotchiOnAndroid.ui.navigation.Screen
import com.hereliesaz.pwnagotchiOnAndroid.ui.screens.HomeScreen
import com.hereliesaz.pwnagotchiOnAndroid.ui.screens.OpwngridScreenNav
import com.hereliesaz.pwnagotchiOnAndroid.ui.screens.PluginsScreenNav
import com.hereliesaz.pwnagotchiOnAndroid.ui.screens.SettingsScreenNav

@Composable
fun MainScreen(
    pwnagotchiUiState: PwnagotchiUiState,
    onDisconnect: () -> Unit,
    onTogglePlugin: (String, Boolean) -> Unit,
    onInstallPlugin: (String) -> Unit,
    onSaveSettings: (String, String, String) -> Unit,
    onReconnect: () -> Unit,
    onFetchLeaderboard: () -> Unit,
) {
    val navController = rememberNavController()
    val items = listOf(
        Screen.Home,
        Screen.Plugins,
        Screen.Opwngrid,
        Screen.Settings
    )

    Row(modifier = Modifier.fillMaxSize()) {
        NavigationRail {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route
            items.forEach { screen ->
                NavigationRailItem(
                    icon = { Icon(screen.icon, contentDescription = null) },
                    label = { Text(screen.route) },
                    selected = currentRoute == screen.route,
                    onClick = {
                        if (screen.route == Screen.Opwngrid.route) {
                            onFetchLeaderboard()
                        }
                        navController.navigate(screen.route) {
                            // Pop up to the start destination of the graph to
                            // avoid building up a large stack of destinations
                            // on the back stack as users select items
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            // Avoid multiple copies of the same destination when
                            // reselecting the same item
                            launchSingleTop = true
                            // Restore state when reselecting a previously selected item
                            restoreState = true
                        }
                    }
                )
            }
        }
        AppNavHost(
            navController = navController,
            pwnagotchiUiState = pwnagotchiUiState,
            onDisconnect = onDisconnect,
            onTogglePlugin = onTogglePlugin,
            onInstallPlugin = onInstallPlugin,
            onSaveSettings = onSaveSettings,
            onReconnect = onReconnect,
            onNavigateToPlugins = { navController.navigate(Screen.Plugins.route) },
            onNavigateToOpwngrid = { navController.navigate(Screen.Opwngrid.route) },
            onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
        )
    }
}

@Composable
fun AppNavHost(
    navController: NavController,
    pwnagotchiUiState: PwnagotchiUiState,
    onDisconnect: () -> Unit,
    onTogglePlugin: (String, Boolean) -> Unit,
    onInstallPlugin: (String) -> Unit,
    onSaveSettings: (String, String, String) -> Unit,
    onReconnect: () -> Unit,
    onNavigateToPlugins: () -> Unit,
    onNavigateToOpwngrid: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    NavHost(
        navController = navController as androidx.navigation.NavHostController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                pwnagotchiUiState = pwnagotchiUiState,
                onReconnect = onReconnect,
                onDisconnect = onDisconnect,
                onNavigateToPlugins = onNavigateToPlugins,
                onNavigateToOpwngrid = onNavigateToOpwngrid,
                onNavigateToSettings = onNavigateToSettings
            )
        }
        composable(Screen.Plugins.route) {
            PluginsScreenNav(pwnagotchiUiState, onTogglePlugin, onInstallPlugin)
        }
        composable(Screen.Opwngrid.route) {
            OpwngridScreenNav(pwnagotchiUiState)
        }
        composable(Screen.Settings.route) {
            SettingsScreenNav(onSaveSettings)
        }
    }
}
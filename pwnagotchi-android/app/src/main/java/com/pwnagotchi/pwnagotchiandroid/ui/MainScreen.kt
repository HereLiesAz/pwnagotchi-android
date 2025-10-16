package com.pwnagotchi.pwnagotchiandroid.ui

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.pwnagotchi.pwnagotchiandroid.PwnagotchiUiState
import com.pwnagotchi.pwnagotchiandroid.ui.navigation.Screen

@Composable
fun MainScreen(
    pwnagotchiUiState: PwnagotchiUiState,
    onConnect: (String) -> Unit,
    onDisconnect: () -> Unit,
    onTogglePlugin: (String, Boolean) -> Unit,
    onInstallPlugin: (String) -> Unit,
    onSaveSettings: (String, String) -> Unit
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
            val currentDestination = navBackStackEntry?.destination
            items.forEach { screen ->
                NavigationRailItem(
                    icon = { Icon(screen.icon, contentDescription = null) },
                    label = { Text(stringResource(screen.title)) },
                    selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                    onClick = {
                        navController.navigate(screen.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
        NavHost(navController = navController, startDestination = Screen.Home.route) {
            composable(Screen.Home.route) {
                HomeScreen(
                    pwnagotchiUiState,
                    onConnect,
                    onDisconnect,
                    onNavigateToPlugins = { navController.navigate(Screen.Plugins.route) },
                    onNavigateToOpwngrid = { navController.navigate(Screen.Opwngrid.route) },
                    onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
                )
            }
            composable(Screen.Plugins.route) { PluginsScreenNav(pwnagotchiUiState, onTogglePlugin, onInstallPlugin) }
            composable(Screen.Opwngrid.route) { OpwngridScreenNav() }
            composable(Screen.Settings.route) { SettingsScreenNav(onSave = onSaveSettings) }
        }
    }
}
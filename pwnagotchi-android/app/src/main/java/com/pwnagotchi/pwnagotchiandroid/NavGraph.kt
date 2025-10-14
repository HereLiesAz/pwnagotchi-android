package com.pwnagotchi.pwnagotchiandroid

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.compose.runtime.collectAsState
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun NavGraph(
    mainViewModel: PwnagotchiViewModel,
    onTogglePlugin: (String, Boolean) -> Unit,
    onInstallPlugin: (String) -> Unit,
    onSaveSettings: (String, String) -> Unit,
    onReconnect: () -> Unit
) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "main") {
        composable("main") {
            val uiState = mainViewModel.uiState.collectAsState().value
            when (uiState) {
                is PwnagotchiUiState.Connecting -> {
                    LoadingScreen(status = uiState.status)
                }
                is PwnagotchiUiState.Connected -> {
                    PwnagotchiScreen(
                        viewModel = mainViewModel,
                        onNavigateToSettings = { navController.navigate("settings") },
                        onNavigateToPlugins = { navController.navigate("plugins") }
                    )
                }
                is PwnagotchiUiState.Disconnected -> {
                    DisconnectedScreen(status = uiState.status, onReconnect = onReconnect)
                }
                is PwnagotchiUiState.Error -> {
                    ErrorScreen(message = uiState.message, onReconnect = onReconnect)
                }
            }
        }
        composable("settings") {
            SettingsScreen(
                onSave = onSaveSettings,
                onBack = { navController.popBackStack() }
            )
        }
        composable("plugins") {
            val uiState = mainViewModel.uiState.collectAsState().value
            if (uiState is PwnagotchiUiState.Connected) {
                PluginsScreen(
                    installedPlugins = uiState.plugins,
                    communityPlugins = uiState.communityPlugins,
                    onTogglePlugin = onTogglePlugin,
                    onInstallPlugin = onInstallPlugin,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}

package com.pwnagotchi.pwnagotchiandroid

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun NavGraph(
    mainViewModel: PwnagotchiViewModel,
    onTogglePlugin: (String, Boolean) -> Unit,
    onSaveSettings: (String, String) -> Unit,
    onReconnect: () -> Unit
) {
    val context = LocalContext.current
    val sharedPreferences = context.getSharedPreferences("pwnagotchi_prefs", Context.MODE_PRIVATE)
    val ipAddress = sharedPreferences.getString("ip_address", null)
    val startDestination = if (ipAddress == null) "settings" else "main"

    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = startDestination) {
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
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}

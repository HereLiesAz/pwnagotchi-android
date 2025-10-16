package com.pwnagotchi.pwnagotchiandroid

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.Text
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun NavGraph(
    mainViewModel: PwnagotchiViewModel,
    onTogglePlugin: (String, Boolean) -> Unit,
    onInstallPlugin: (String) -> Unit,
    onSaveSettings: (String) -> Unit,
    onReconnect: () -> Unit
) {
    val context = LocalContext.current
    val sharedPreferences = context.getSharedPreferences("pwnagotchi_prefs", Context.MODE_PRIVATE)
    val host = sharedPreferences.getString("host", null)
    val startDestination = if (host == null) Screen.Settings.route else Screen.Home.route

    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = startDestination) {
        composable(Screen.Home.route) {
            val uiState = mainViewModel.uiState.collectAsState().value
            when (uiState) {
                is PwnagotchiUiState.Connecting -> {
                    LoadingScreen(status = uiState.status)
                }
                is PwnagotchiUiState.Connected -> {
                    PwnagotchiScreen(
                        viewModel = mainViewModel,
                        onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                        onNavigateToPlugins = { navController.navigate(Screen.Plugins.route) },
                        onNavigateToOpwngrid = { navController.navigate(Screen.Opwngrid.route) }
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
        composable(Screen.Settings.route) {
            SettingsScreen(
                onSave = onSaveSettings,
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.Plugins.route) {
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
        composable(Screen.Opwngrid.route) {
            val opwngridViewModel: OpwngridViewModel = viewModel(factory = OpwngridViewModelFactory(context))
            OpwngridScreen(viewModel = opwngridViewModel)
        }
    }
}

package com.hereliesaz.pwnagotchiOnAndroid.ui.screens

import androidx.compose.runtime.Composable
import com.hereliesaz.pwnagotchiOnAndroid.PwnagotchiUiState
import com.hereliesaz.pwnagotchiOnAndroid.ui.ConnectingScreen
import com.hereliesaz.pwnagotchiOnAndroid.ui.DisconnectedScreen
import com.hereliesaz.pwnagotchiOnAndroid.ui.ErrorScreen
import com.hereliesaz.pwnagotchiOnAndroid.PwnagotchiScreen

@Composable
fun HomeScreen(
    pwnagotchiUiState: PwnagotchiUiState,
    onReconnect: () -> Unit,
    onDisconnect: () -> Unit,
    onNavigateToPlugins: () -> Unit,
    onNavigateToOpwngrid: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    when (pwnagotchiUiState) {
        is PwnagotchiUiState.Connecting -> ConnectingScreen(pwnagotchiUiState.message)
        is PwnagotchiUiState.Connected -> PwnagotchiScreen(
            uiState = pwnagotchiUiState,
            onDisconnect = onDisconnect,
            onNavigateToPlugins = onNavigateToPlugins,
            onNavigateToOpwngrid = onNavigateToOpwngrid,
            onNavigateToSettings = onNavigateToSettings
        )
        is PwnagotchiUiState.Disconnected -> com.hereliesaz.pwnagotchiOnAndroid.DisconnectedScreen(pwnagotchiUiState.message, onReconnect)
        is PwnagotchiUiState.Error -> com.hereliesaz.pwnagotchiOnAndroid.ErrorScreen(pwnagotchiUiState.message, onReconnect)
    }
}
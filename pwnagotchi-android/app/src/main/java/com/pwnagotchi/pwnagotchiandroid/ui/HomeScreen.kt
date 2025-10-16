package com.pwnagotchi.pwnagotchiandroid.ui

import androidx.compose.runtime.Composable
import com.pwnagotchi.pwnagotchiandroid.PwnagotchiScreen
import com.pwnagotchi.pwnagotchiandroid.PwnagotchiUiState

@Composable
fun HomeScreen(
    uiState: PwnagotchiUiState,
    onConnect: (String) -> Unit,
    onDisconnect: () -> Unit,
    onNavigateToPlugins: () -> Unit,
    onNavigateToOpwngrid: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    when (uiState) {
        is PwnagotchiUiState.Connected -> PwnagotchiScreen(
            uiState = uiState,
            onDisconnect = onDisconnect,
            onNavigateToPlugins = onNavigateToPlugins,
            onNavigateToOpwngrid = onNavigateToOpwngrid,
            onNavigateToSettings = onNavigateToSettings
        )
        is PwnagotchiUiState.Connecting -> ConnectingScreen(uiState.status)
        is PwnagotchiUiState.Disconnected -> DisconnectedScreen(onConnect)
        is PwnagotchiUiState.Error -> ErrorScreen(uiState.message, onConnect)
    }
}
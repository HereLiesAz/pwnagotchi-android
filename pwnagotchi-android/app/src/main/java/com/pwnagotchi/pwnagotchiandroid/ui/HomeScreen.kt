package com.pwnagotchi.pwnagotchiandroid.ui

import androidx.compose.runtime.Composable
import com.pwnagotchi.pwnagotchiandroid.PwnagotchiUiState
import com.pwnagotchi.pwnagotchiandroid.PwnagotchiScreen
import com.pwnagotchi.pwnagotchiandroid.DisconnectedScreen
import com.pwnagotchi.pwnagotchiandroid.LoadingScreen
import com.pwnagotchi.pwnagotchiandroid.ErrorScreen

@Composable
fun HomeScreen(
    pwnagotchiUiState: PwnagotchiUiState,
    onDisconnect: () -> Unit,
    onNavigateToPlugins: () -> Unit,
    onNavigateToOpwngrid: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onReconnect: () -> Unit
) {
    when (pwnagotchiUiState) {
        is PwnagotchiUiState.Connected -> {
            PwnagotchiScreen(
                uiState = pwnagotchiUiState,
                onDisconnect = onDisconnect,
                onNavigateToPlugins = onNavigateToPlugins,
                onNavigateToOpwngrid = onNavigateToOpwngrid,
                onNavigateToSettings = onNavigateToSettings
            )
        }
        is PwnagotchiUiState.Disconnected -> {
            DisconnectedScreen(
                status = pwnagotchiUiState.status,
                onReconnect = onReconnect
            )
        }
        is PwnagotchiUiState.Connecting -> {
            LoadingScreen(pwnagotchiUiState.status)
        }
        is PwnagotchiUiState.Error -> {
            ErrorScreen(
                message = pwnagotchiUiState.message,
                onReconnect = onReconnect
            )
        }
    }
}
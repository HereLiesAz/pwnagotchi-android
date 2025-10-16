package com.pwnagotchi.pwnagotchiOnAndroid.ui

import androidx.compose.runtime.Composable
import com.pwnagotchi.pwnagotchiOnAndroid.PwnagotchiUiState
import com.pwnagotchi.pwnagotchiOnAndroid.PwnagotchiScreen
import com.pwnagotchi.pwnagotchiOnAndroid.DisconnectedScreen
import com.pwnagotchi.pwnagotchiOnAndroid.LoadingScreen
import com.pwnagotchi.pwnagotchiOnAndroid.ErrorScreen

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

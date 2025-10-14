package com.pwnagotchi.pwnagotchiandroid

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState

@Composable
fun PwnagotchiScreen(
    viewModel: PwnagotchiViewModel,
    onNavigateToSettings: () -> Unit,
    onNavigateToPlugins: () -> Unit
) {
    val uiState = viewModel.uiState.collectAsState().value
    Column {
        if (uiState is PwnagotchiUiState.Connected) {
            Text(text = uiState.face)
            Text(text = uiState.status)
        }
        Button(onClick = onNavigateToSettings) {
            Text("Settings")
        }
        Button(onClick = onNavigateToPlugins) {
            Text("Plugins")
        }
    }
}

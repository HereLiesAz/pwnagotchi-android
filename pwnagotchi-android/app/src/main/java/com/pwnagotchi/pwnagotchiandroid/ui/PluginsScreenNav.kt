package com.pwnagotchi.pwnagotchiandroid.ui

import androidx.compose.runtime.Composable
import com.pwnagotchi.pwnagotchiandroid.PwnagotchiViewModel

@Composable
fun PluginsScreenNav(
    mainViewModel: PwnagotchiViewModel,
    onTogglePlugin: (String, Boolean) -> Unit,
    onInstallPlugin: (String) -> Unit
) {
    PluginsScreen(
        uiState = mainViewModel.uiState.value,
        onTogglePlugin = onTogglePlugin,
        onInstallPlugin = onInstallPlugin
    )
}

package com.pwnagotchi.pwnagotchiOnAndroid.ui

import androidx.compose.runtime.Composable
import com.pwnagotchi.pwnagotchiOnAndroid.PwnagotchiUiState
import com.pwnagotchi.pwnagotchiOnAndroid.PluginsScreen

@Composable
fun PluginsScreenNav(
    pwnagotchiUiState: PwnagotchiUiState,
    onTogglePlugin: (String, Boolean) -> Unit,
    onInstallPlugin: (String) -> Unit
) {
    if (pwnagotchiUiState is PwnagotchiUiState.Connected) {
        PluginsScreen(
            plugins = pwnagotchiUiState.plugins,
            communityPlugins = pwnagotchiUiState.communityPlugins,
            onTogglePlugin = onTogglePlugin,
            onInstallPlugin = onInstallPlugin
        )
    }
}

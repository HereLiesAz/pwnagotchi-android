package com.hereliesaz.pwnagotchiOnAndroid.ui.screens

import androidx.compose.runtime.Composable
import com.hereliesaz.pwnagotchiOnAndroid.OpwngridScreen
import com.hereliesaz.pwnagotchiOnAndroid.OpwngridUiState
import com.hereliesaz.pwnagotchiOnAndroid.PwnagotchiUiState

@Composable
fun OpwngridScreenNav(
    pwnagotchiUiState: PwnagotchiUiState
) {
    if (pwnagotchiUiState is PwnagotchiUiState.Connected) {
        OpwngridScreen(OpwngridUiState.Success(pwnagotchiUiState.leaderboard))
    }
}
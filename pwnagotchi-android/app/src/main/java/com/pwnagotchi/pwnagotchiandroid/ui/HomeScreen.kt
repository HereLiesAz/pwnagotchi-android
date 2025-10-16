package com.pwnagotchi.pwnagotchiandroid.ui

import androidx.compose.runtime.Composable
import com.pwnagotchi.pwnagotchiandroid.PwnagotchiViewModel

@Composable
fun HomeScreen(
    mainViewModel: PwnagotchiViewModel,
    onReconnect: () -> Unit
) {
    // This will eventually hold the content of the home screen.
    // For now, it will just display the main UI.
    PwnagotchiScreen(
        uiState = mainViewModel.uiState.value,
        onReconnect = onReconnect
    )
}

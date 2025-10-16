package com.pwnagotchi.pwnagotchiandroid.ui

import androidx.compose.runtime.Composable
import com.pwnagotchi.pwnagotchiandroid.PwnagotchiViewModel

@Composable
fun SettingsScreenNav(
    mainViewModel: PwnagotchiViewModel,
    onSaveSettings: (String) -> Unit
) {
    SettingsScreen(
        uiState = mainViewModel.uiState.value,
        onSave = onSaveSettings
    )
}

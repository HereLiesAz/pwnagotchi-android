package com.pwnagotchi.pwnagotchiandroid.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.pwnagotchi.pwnagotchiandroid.OpwngridScreen
import com.pwnagotchi.pwnagotchiandroid.PwnagotchiViewModel

@Composable
fun OpwngridScreenNav(
    mainViewModel: PwnagotchiViewModel
) {
    val uiState by mainViewModel.opwngridViewModel.uiState.collectAsState()
    OpwngridScreen(
        uiState = uiState
    )
}

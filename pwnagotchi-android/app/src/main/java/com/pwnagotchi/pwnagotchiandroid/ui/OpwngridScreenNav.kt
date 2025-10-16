package com.pwnagotchi.pwnagotchiandroid.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pwnagotchi.pwnagotchiandroid.OpwngridScreen
import com.pwnagotchi.pwnagotchiandroid.OpwngridViewModel
import com.pwnagotchi.pwnagotchiandroid.OpwngridViewModelFactory

@Composable
fun OpwngridScreenNav() {
    val context = LocalContext.current
    val opwngridViewModel: OpwngridViewModel = viewModel(factory = OpwngridViewModelFactory(context))
    val uiState by opwngridViewModel.uiState.collectAsState()
    OpwngridScreen(
        uiState = uiState
    )
}
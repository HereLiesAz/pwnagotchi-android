package com.pwnagotchi.pwnagotchiandroid

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.res.painterResource

@Composable
fun PwnagotchiScreen(
    viewModel: PwnagotchiViewModel,
    onNavigateToSettings: () -> Unit,
    onNavigateToPlugins: () -> Unit
) {
    val uiState = viewModel.uiState.collectAsState().value
    Column {
        if (uiState is PwnagotchiUiState.Connected) {
            // TODO: Load custom themes and faces from user storage
            val face = when (uiState.face) {
                "(·•᷄_•᷅ ·)" -> R.drawable.pwnagotchi_sad
                "(·•ᴗ• ·)" -> R.drawable.pwnagotchi_happy
                else -> R.drawable.pwnagotchi_neutral
            }
            Crossfade(targetState = face) { faceResId ->
                Image(
                    painter = painterResource(id = faceResId),
                    contentDescription = "Pwnagotchi Face"
                )
            }
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

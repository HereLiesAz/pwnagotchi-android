package com.pwnagotchi.pwnagotchiOnAndroid

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.pwnagotchi.pwnagotchiOnAndroid.viewmodels.PwnagotchiViewModel

@Composable
fun PwnagotchiScreen(
    viewModel: PwnagotchiViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    Column {
        val face = when (uiState) {
            is PwnagotchiUiState.Connected -> when ((uiState as PwnagotchiUiState.Connected).face) {
                "(·•᷄_•᷅ ·)" -> R.drawable.pwnagotchi_sad
                "(·•ᴗ• ·)" -> R.drawable.pwnagotchi_happy
                else -> R.drawable.pwnagotchi_neutral
            }
            else -> R.drawable.pwnagotchi_neutral
        }
        Crossfade(targetState = face) { faceResId ->
            Image(
                painter = painterResource(id = faceResId),
                contentDescription = stringResource(id = R.string.pwnagotchi_face)
            )
        }
        val statusText = when (uiState) {
            is PwnagotchiUiState.Connected -> (uiState as PwnagotchiUiState.Connected).status
            is PwnagotchiUiState.Connecting -> (uiState as PwnagotchiUiState.Connecting).status
            is PwnagotchiUiState.Disconnected -> (uiState as PwnagotchiUiState.Disconnected).status
            is PwnagotchiUiState.Error -> (uiState as PwnagotchiUiState.Error).message
        }
        Text(text = statusText)
        Button(onClick = { viewModel.disconnect() }) {
            Text("Disconnect")
        }
    }
}

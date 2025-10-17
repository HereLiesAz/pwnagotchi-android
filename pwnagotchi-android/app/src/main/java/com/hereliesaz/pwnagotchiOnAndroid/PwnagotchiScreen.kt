package com.hereliesaz.pwnagotchiOnAndroid

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource

@Composable
fun PwnagotchiScreen(
    uiState: PwnagotchiUiState.Connected,
    onDisconnect: () -> Unit,
    onNavigateToPlugins: () -> Unit,
    onNavigateToOpwngrid: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    Column {
        val face = when (uiState.face) {
            "(·•᷄_•᷅ ·)" -> R.drawable.pwnagotchi_sad
            "(·•ᴗ• ·)" -> R.drawable.pwnagotchi_happy
            else -> R.drawable.pwnagotchi_neutral
        }
        Crossfade(targetState = face) { faceResId ->
            Image(
                painter = painterResource(id = faceResId),
                contentDescription = stringResource(id = R.string.pwnagotchi_face)
            )
        }
        Text(text = uiState.status)
        Button(onClick = onDisconnect) {
            Text("Disconnect")
        }
        Button(onClick = onNavigateToSettings) {
            Text(stringResource(id = R.string.settings))
        }
        Button(onClick = onNavigateToOpwngrid) {
            Text(stringResource(id = R.string.opwngrid))
        }
        Button(onClick = onNavigateToPlugins) {
            Text(stringResource(id = R.string.plugins))
        }
    }
}

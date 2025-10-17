package com.hereliesaz.pwnagotchiOnAndroid.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.hereliesaz.pwnagotchiOnAndroid.core.Constants

@Composable
fun ErrorScreen(message: String, onConnect: (String) -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = message)
        Button(onClick = { onConnect(Constants.DEFAULT_PWNAGOTCHI_IP) }) {
            Text("Reconnect")
        }
    }
}

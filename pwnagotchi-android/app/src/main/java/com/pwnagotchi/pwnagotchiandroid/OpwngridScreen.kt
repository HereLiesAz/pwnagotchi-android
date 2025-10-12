package com.pwnagotchi.pwnagotchiandroid

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun OpwngridScreen(
    leaderboard: List<LeaderboardEntry>,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("oPwngrid Leaderboard", style = androidx.compose.material3.MaterialTheme.typography.headlineMedium)
        LazyColumn(
            modifier = Modifier.weight(1f)
        ) {
            items(leaderboard) { entry ->
                Text("${entry.name}: ${entry.pwns}")
            }
        }
        Button(onClick = onBack) {
            Text("Back")
        }
    }
}

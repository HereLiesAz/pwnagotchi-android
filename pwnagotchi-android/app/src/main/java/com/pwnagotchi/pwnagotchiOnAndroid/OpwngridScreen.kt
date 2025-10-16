package com.pwnagotchi.pwnagotchiOnAndroid

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun OpwngridScreen(
    viewModel: OpwngridViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        when (val state = uiState) {
            is OpwngridUiState.Loading -> {
                CircularProgressIndicator()
            }
            is OpwngridUiState.Success -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.leaderboard) { entry ->
                        LeaderboardItem(entry = entry)
                    }
                }
            }
            is OpwngridUiState.Error -> {
                Text(text = "Error: ${state.message}")
            }
        }
    }
}

@Composable
fun LeaderboardItem(entry: LeaderboardEntry) {
    Column {
        Text(text = "Rank: ${entry.rank}")
        Text(text = "Username: ${entry.name}")
        Text(text = "Pwned: ${entry.handshakes}")
    }
}

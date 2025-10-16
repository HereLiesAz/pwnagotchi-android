package com.pwnagotchi.pwnagotchiandroid.widgets

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.provideContent
import androidx.glance.text.Text
import androidx.glance.GlanceId
import com.pwnagotchi.pwnagotchiandroid.LeaderboardEntry
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json

class LeaderboardWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val repository = WidgetStateRepository(context)
            val leaderboardJson by repository.leaderboard.collectAsState(initial = runBlocking { repository.leaderboard.first() })
            val leaderboard = Json.decodeFromString<List<LeaderboardEntry>>(leaderboardJson)

            Content(leaderboard = leaderboard)
        }
    }

    @Composable
    private fun Content(leaderboard: List<LeaderboardEntry>) {
        LazyColumn {
            items(leaderboard.size) { index ->
                val entry = leaderboard[index]
                Text(text = "${entry.rank}. ${entry.name} - ${entry.handshakes}")
            }
        }
    }
}
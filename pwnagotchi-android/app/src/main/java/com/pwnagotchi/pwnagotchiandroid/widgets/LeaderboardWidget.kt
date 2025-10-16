package com.pwnagotchi.pwnagotchiandroid.widgets

import androidx.compose.runtime.Composable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.currentState
import androidx.glance.layout.LazyColumn
import androidx.glance.text.Text
import kotlinx.serialization.json.Json
import com.pwnagotchi.pwnagotchiandroid.LeaderboardEntry

class LeaderboardWidget : GlanceAppWidget() {
    @Composable
    override fun Content() {
        val state = currentState<WidgetState>()
        val leaderboard = Json.decodeFromString<List<LeaderboardEntry>>(state.leaderboard)

        LazyColumn {
            items(leaderboard) { entry ->
                Text(text = "${entry.rank}. ${entry.name} - ${entry.handshakes} handshakes")
            }
        }
    }
}

class LeaderboardWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = LeaderboardWidget()
}

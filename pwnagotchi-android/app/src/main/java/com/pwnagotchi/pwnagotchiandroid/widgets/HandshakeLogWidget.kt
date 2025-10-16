package com.pwnagotchi.pwnagotchiandroid.widgets

import androidx.compose.runtime.Composable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.currentState
import androidx.glance.layout.LazyColumn
import androidx.glance.text.Text
import kotlinx.serialization.json.Json

class HandshakeLogWidget : GlanceAppWidget() {
    @Composable
    override fun Content() {
        val state = currentState<WidgetState>()
        val handshakes = try {
            Json.decodeFromString<List<com.pwnagotchi.pwnagotchiandroid.Handshake>>(state.handshakes)
        } catch (e: Exception) {
            emptyList()
        }

        LazyColumn {
            items(handshakes) { handshake ->
                Text(text = "${handshake.ap} - ${handshake.sta}")
            }
        }
    }
}

class HandshakeLogWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = HandshakeLogWidget()
}

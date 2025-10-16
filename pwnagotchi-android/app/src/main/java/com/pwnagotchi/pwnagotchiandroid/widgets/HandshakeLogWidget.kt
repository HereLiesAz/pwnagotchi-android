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
import com.pwnagotchi.pwnagotchiandroid.Handshake
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json

class HandshakeLogWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val repository = WidgetStateRepository(context)
            val handshakesJson by repository.handshakes.collectAsState(initial = runBlocking { repository.handshakes.first() })
            val handshakes = Json.decodeFromString<List<Handshake>>(handshakesJson)

            Content(handshakes = handshakes)
        }
    }

    @Composable
    private fun Content(handshakes: List<Handshake>) {
        LazyColumn {
            items(handshakes.size) { index ->
                val handshake = handshakes[index]
                Text(text = "${handshake.ap} - ${handshake.sta}")
            }
        }
    }
}
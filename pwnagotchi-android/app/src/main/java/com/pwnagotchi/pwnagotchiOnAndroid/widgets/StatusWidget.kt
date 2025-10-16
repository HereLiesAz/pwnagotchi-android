package com.pwnagotchi.pwnagotchiOnAndroid.widgets

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.text.Text

class StatusWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val repository = WidgetStateRepository(context)
            val face by repository.face.collectAsState(initial = "")
            val message by repository.message.collectAsState(initial = "")

            Content(face = face, message = message)
        }
    }

    @Composable
    private fun Content(face: String, message: String) {
        Text(text = "$face $message")
    }
}

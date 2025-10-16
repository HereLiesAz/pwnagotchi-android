package com.pwnagotchi.pwnagotchiandroid.widgets

import androidx.compose.runtime.Composable
import androidx.glance.Button
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.actionRunCallback
import com.pwnagotchi.pwnagotchiandroid.ReconnectCallback

class QuickActionsWidget : GlanceAppWidget() {
    @Composable
    override fun Content() {
        Button(
            text = "Reconnect",
            onClick = actionRunCallback<ReconnectCallback>()
        )
    }
}

class QuickActionsWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = QuickActionsWidget()
}

package com.pwnagotchi.pwnagotchiandroid.widgets

import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver

class HandshakeLogWidget : GlanceAppWidget() {
    // TODO: Implement widget
}

class HandshakeLogWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = HandshakeLogWidget()
}

package com.pwnagotchi.pwnagotchiandroid.widgets

import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver

class LeaderboardWidget : GlanceAppWidget() {
    // TODO: Implement widget
}

class LeaderboardWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = LeaderboardWidget()
}

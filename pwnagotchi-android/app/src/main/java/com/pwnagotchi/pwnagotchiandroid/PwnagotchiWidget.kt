package com.pwnagotchi.pwnagotchiandroid

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews

class PwnagotchiWidget : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId, "Status: Unknown")
        }
    }

    companion object {
        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int,
            status: String
        ) {
            val intent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)

            val views = RemoteViews(context.packageName, R.layout.pwnagotchi_widget)
            views.setOnClickPendingIntent(R.id.widget_layout, pendingIntent)

            val faceRegex = Regex("""\(.+\)""")
            val faceMatch = faceRegex.find(status)
            val face = faceMatch?.value ?: "(·•᷄_•᷅ ·)"

            views.setTextViewText(R.id.widget_face, face)
            views.setTextViewText(R.id.widget_status, status)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}

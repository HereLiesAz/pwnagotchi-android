package com.pwnagotchi.pwnagotchiandroid

import android.content.Context
import android.content.Intent
import androidx.glance.appwidget.action.ActionCallback

class ReconnectCallback : ActionCallback {
    override suspend fun onRun(context: Context, glanceId: androidx.glance.GlanceId, parameters: androidx.glance.action.ActionParameters) {
        val intent = Intent(context, PwnagotchiService::class.java).apply {
            action = "com.pwnagotchi.pwnagotchiandroid.RECONNECT"
        }
        context.startService(intent)
    }
}

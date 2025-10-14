package com.pwnagotchi.pwnagotchiandroid

import android.app.Notification
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class BettercapService : Service() {

    private val binder = LocalBinder()
    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)
    private val _uiState = MutableStateFlow<String>("Waiting for bettercap...")
    val uiState: StateFlow<String> = _uiState

    inner class LocalBinder : Binder() {
        fun getService(): BettercapService = this@BettercapService
    }

    override fun onCreate() {
        super.onCreate()
        // Notification channel is created by the helper
    }

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = NotificationHelper.createNotification(this, "bettercap_service_channel", "Bettercap Service Channel", "Bettercap service is running")
        startForeground(1, notification)
        startBettercap()
        return START_STICKY
    }

    private fun startBettercap() {
        serviceScope.launch {
            val sharedPreferences = getSharedPreferences("pwnagotchi_prefs", Context.MODE_PRIVATE)
            val unsafeInterfaceName = sharedPreferences.getString("interface_name", "wlan0") ?: "wlan0"
            val interfaceName = unsafeInterfaceName.filter { it.isLetterOrDigit() }

            val outputBuffer = mutableListOf<String>()
            _uiState.value = "" // Start with empty output
            val streamOutput = object : MutableList<String> by mutableListOf() {
                override fun add(element: String): Boolean {
                    outputBuffer.add(element)
                    if (outputBuffer.size > 100) {
                        outputBuffer.removeAt(0)
                    }
                    _uiState.value = outputBuffer.joinToString("\n")
                    updateWidget(outputBuffer.joinToString("\n"))
                    return true
                }
            }
            Shell.cmd("bettercap -iface $interfaceName -caplet pwnagotchi-auto")
                .to(streamOutput, streamOutput)
                .submit()
        }
    }

    private fun updateWidget(status: String) {
        val appWidgetManager = AppWidgetManager.getInstance(this)
        val thisAppWidget = ComponentName(this, PwnagotchiWidget::class.java)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(thisAppWidget)
        for (appWidgetId in appWidgetIds) {
            PwnagotchiWidget.updateAppWidget(this, appWidgetManager, appWidgetId, status)
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        serviceJob.cancel()
        stopForeground(true)
        stopSelf()
    }
}

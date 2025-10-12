package com.pwnagotchi.pwnagotchiandroid

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.net.URI

class PwnagotchiService : Service() {

    private val binder = LocalBinder()
    private var webSocketClient: WebSocketClient? = null
    private val _uiState = MutableStateFlow<PwnagotchiUiState>(PwnagotchiUiState.Disconnected("Not connected"))
    val uiState: StateFlow<PwnagotchiUiState> = _uiState

    private val serviceScope = CoroutineScope(Dispatchers.IO + Job())
    private var reconnectionJob: Job? = null
    private var currentUri: URI? = null

    inner class LocalBinder : Binder() {
        fun getService(): PwnagotchiService = this@PwnagotchiService
    }

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = createNotification("Pwnagotchi service is running")
        startForeground(1, notification)
        return START_STICKY
    }

    fun connect(uri: URI) {
        currentUri = uri
        reconnectionJob?.cancel() // Cancel any previous reconnection attempts
        webSocketClient?.close() // Close any existing connection

        _uiState.value = PwnagotchiUiState.Connecting("Connecting to $uri...")
        try {
            webSocketClient = object : WebSocketClient(uri) {
                override fun onOpen(handshakedata: ServerHandshake?) {
                    _uiState.value = PwnagotchiUiState.Connected("Connected")
                    updateNotification("Connected to Pwnagotchi")
                }

                override fun onMessage(message: String?) {
                    _uiState.value = PwnagotchiUiState.Connected(message ?: "")
                }

                override fun onClose(code: Int, reason: String?, remote: Boolean) {
                    _uiState.value = PwnagotchiUiState.Disconnected("Connection closed. Reconnecting...")
                    updateNotification("Connection closed. Reconnecting...")
                    scheduleReconnect()
                }

                override fun onError(ex: Exception?) {
                    _uiState.value = PwnagotchiUiState.Error(ex?.message ?: "Unknown error")
                    scheduleReconnect()
                }
            }
            webSocketClient?.connect()
        } catch (e: Exception) {
            _uiState.value = PwnagotchiUiState.Error(e.message ?: "Unknown connection error")
            scheduleReconnect()
        }
    }

    fun disconnect() {
        reconnectionJob?.cancel()
        webSocketClient?.close()
        _uiState.value = PwnagotchiUiState.Disconnected("Disconnected by user")
        updateNotification("Disconnected")
    }

    private fun scheduleReconnect() {
        if (reconnectionJob?.isActive == true) return
        reconnectionJob = serviceScope.launch {
            var delayMs = 1000L
            val maxDelayMs = 60000L
            while (true) {
                delay(delayMs)
                currentUri?.let {
                    _uiState.value = PwnagotchiUiState.Connecting("Reconnecting...")
                    webSocketClient?.reconnect()
                }
                delayMs = (delayMs * 2).coerceAtMost(maxDelayMs)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        reconnectionJob?.cancel()
        webSocketClient?.close()
        serviceScope.coroutineContext.cancel()
    }

    private fun createNotification(contentText: String): Notification {
        val channelId = "pwnagotchi_service_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Pwnagotchi Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Pwnagotchi Service")
            .setContentText(contentText)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .build()
    }

    private fun updateNotification(contentText: String) {
        val notification = createNotification(contentText)
        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(1, notification)
    }
}

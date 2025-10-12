package com.pwnagotchi.pwnagotchiandroid

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
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
import org.json.JSONObject
import java.net.URI

class PwnagotchiService : Service() {

    private val binder = LocalBinder()
    private var webSocketClient: WebSocketClient? = null
    private val _uiState = MutableStateFlow<PwnagotchiUiState>(PwnagotchiUiState.Disconnected("Not connected"))
    val uiState: StateFlow<PwnagotchiUiState> = _uiState

    private val serviceScope = CoroutineScope(Dispatchers.IO + Job())
    private var reconnectionJob: Job? = null
    private var currentUri: URI? = null
    private val handshakes = mutableListOf<Handshake>()
    private val plugins = mutableListOf<Plugin>()
    private var face = "(·•᷄_•᷅ ·)"

    inner class LocalBinder : Binder() {
        fun getService(): PwnagotchiService = this@PwnagotchiService
    }

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = createNotification("Pwnagotchi service is running", "pwnagotchi_service_channel")
        startForeground(1, notification)
        val sharedPreferences = getSharedPreferences("pwnagotchi_prefs", Context.MODE_PRIVATE)
        val ipAddress = sharedPreferences.getString("ip_address", null)
        if (ipAddress != null) {
            connect(URI("ws://$ipAddress:8765"))
        }
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
                    _uiState.value = PwnagotchiUiState.Connected("Connected", handshakes, plugins, face)
                    updateNotification("Connected to Pwnagotchi")
                    listPlugins()
                }

                override fun onMessage(message: String?) {
                    val json = JSONObject(message)
                    when (json.getString("type")) {
                        "ui_update" -> {
                            val data = json.getJSONObject("data")
                            face = data.getString("face")
                            val notificationText = "CH: ${data.getString("channel")} | APS: ${data.getString("aps")} | UP: ${data.getString("uptime")} | PWND: ${data.getString("shakes")} | MODE: ${data.getString("mode")}"
                            _uiState.value = PwnagotchiUiState.Connected(notificationText, handshakes, plugins, face)
                            updateNotification(notificationText)
                        }
                        "handshake" -> {
                            val data = json.getJSONObject("data")
                            val handshake = Handshake(
                                ap = data.getJSONObject("ap").getString("hostname"),
                                sta = data.getJSONObject("sta").getString("mac"),
                                filename = data.getString("filename")
                            )
                            handshakes.add(handshake)
                            _uiState.value = PwnagotchiUiState.Connected("New handshake captured!", handshakes, plugins, face)
                            showHandshakeNotification(handshake)
                        }
                        "plugin_list" -> {
                            val data = json.getJSONArray("data")
                            plugins.clear()
                            for (i in 0 until data.length()) {
                                val pluginJson = data.getJSONObject(i)
                                val plugin = Plugin(
                                    name = pluginJson.getString("name"),
                                    enabled = pluginJson.getBoolean("enabled")
                                )
                                plugins.add(plugin)
                            }
                            _uiState.value = PwnagotchiUiState.Connected("Plugins loaded", handshakes, plugins, face)
                        }
                    }
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

    fun listPlugins() {
        webSocketClient?.send("{\"command\": \"list_plugins\"}")
    }

    fun togglePlugin(pluginName: String, enabled: Boolean) {
        webSocketClient?.send("{\"command\": \"toggle_plugin\", \"plugin_name\": \"$pluginName\", \"enabled\": $enabled}")
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

    private fun createNotification(contentText: String, channelId: String, channelName: String = "Pwnagotchi Service Channel", importance: Int = NotificationManager.IMPORTANCE_DEFAULT): Notification {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                channelName,
                importance
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Pwnagotchi Status")
            .setContentText(contentText)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .build()
    }

    private fun updateNotification(contentText: String) {
        val notification = createNotification(contentText, "pwnagotchi_service_channel")
        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(1, notification)
    }

    private fun showHandshakeNotification(handshake: Handshake) {
        val contentText = "Captured handshake from ${handshake.ap}"
        val notification = createNotification(contentText, "handshake_channel", "Handshake Notifications", NotificationManager.IMPORTANCE_HIGH)
        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(2, notification)
    }
}

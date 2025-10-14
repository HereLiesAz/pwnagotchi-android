package com.pwnagotchi.pwnagotchiandroid

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import org.json.JSONObject
import java.net.URI

// TODO: Acknowledge the suggestion to split this large PR into smaller, focused PRs in the future.
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
    private val communityPlugins = mutableListOf<CommunityPlugin>()
    private var face = "(·•᷄_•᷅ ·)"

    inner class LocalBinder : Binder() {
        fun getService(): PwnagotchiService = this@PwnagotchiService
    }

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = NotificationHelper.createNotification(this, "pwnagotchi_service_channel", "Pwnagotchi Service Channel", "Pwnagotchi service is running")
        startForeground(1, notification)
        val sharedPreferences = getSharedPreferences("pwnagotchi_prefs", Context.MODE_PRIVATE)
        val ipAddress = sharedPreferences.getString("ip_address", null)
        if (ipAddress != null) {
            // TODO: Use wss for secure WebSocket connections. This will require server-side changes.
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
                    _uiState.value = PwnagotchiUiState.Connected("Connected", handshakes, plugins, face, emptyList(), communityPlugins)
                    updateNotification("Connected to Pwnagotchi")
                    listPlugins()
                    getCommunityPlugins()
                }

                override fun onMessage(message: String?) {
                    val json = JSONObject(message)
                    when (json.getString("type")) {
                        "ui_update" -> {
                            val data = json.getJSONObject("data")
                            face = data.getString("face")
                            val notificationText = "CH: ${data.getString("channel")} | APS: ${data.getString("aps")} | UP: ${data.getString("uptime")} | PWND: ${data.getString("shakes")} | MODE: ${data.getString("mode")}"
                            _uiState.value = PwnagotchiUiState.Connected(notificationText, handshakes, plugins, face, emptyList(), communityPlugins)
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
                            _uiState.value = PwnagotchiUiState.Connected("New handshake captured!", handshakes, plugins, face, emptyList(), communityPlugins)
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
                            _uiState.value = PwnagotchiUiState.Connected("Plugins loaded", handshakes, plugins, face, emptyList(), communityPlugins)
                        }
                        "community_plugin_list" -> {
                            val data = json.getJSONArray("data")
                            communityPlugins.clear()
                            for (i in 0 until data.length()) {
                                val pluginJson = data.getJSONObject(i)
                                val plugin = CommunityPlugin(
                                    name = pluginJson.getString("name"),
                                    description = pluginJson.getString("description")
                                )
                                communityPlugins.add(plugin)
                            }
                            _uiState.value = PwnagotchiUiState.Connected("Community plugins loaded", handshakes, plugins, face, emptyList(), communityPlugins)
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

    private fun isWebSocketOpen(): Boolean {
        return webSocketClient?.isOpen == true
    }

    fun listPlugins() {
        if (isWebSocketOpen()) {
            webSocketClient?.send("{\"command\": \"list_plugins\"}")
        } else {
            // Optionally handle the case when the WebSocket is not open
            // e.g., log, show a message, or attempt reconnection
        }
    }

    fun togglePlugin(pluginName: String, enabled: Boolean) {
        if (isWebSocketOpen()) {
            webSocketClient?.send("{\"command\": \"toggle_plugin\", \"plugin_name\": \"$pluginName\", \"enabled\": $enabled}")
        }
    }

    fun getCommunityPlugins() {
        if (isWebSocketOpen()) {
            webSocketClient?.send("{\"command\": \"get_community_plugins\"}")
        }
    }

    private fun scheduleReconnect() {
        if (reconnectionJob?.isActive == true) return
        var attempts = 0
        reconnectionJob = serviceScope.launch {
            var delayMs = 1000L
            val maxDelayMs = 60000L
            while (attempts < 5) {
                delay(delayMs)
                currentUri?.let {
                    _uiState.value = PwnagotchiUiState.Connecting("Reconnecting...")
                    webSocketClient?.reconnect()
                }
                delayMs = (delayMs * 2).coerceAtMost(maxDelayMs)
                attempts++
            }
            _uiState.value = PwnagotchiUiState.Error("Failed to reconnect after 5 attempts.")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        webSocketClient?.close()
    }

    private fun updateNotification(contentText: String) {
        val notification = NotificationHelper.createNotification(this, "pwnagotchi_service_channel", "Pwnagotchi Service Channel", contentText)
        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(1, notification)
    }

    private fun showHandshakeNotification(handshake: Handshake) {
        val contentText = "Captured handshake from ${handshake.ap}"
        val notification = NotificationHelper.createNotification(this, "handshake_channel", "Handshake Notifications", contentText, NotificationManager.IMPORTANCE_HIGH)
        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(2, notification)
    }
}

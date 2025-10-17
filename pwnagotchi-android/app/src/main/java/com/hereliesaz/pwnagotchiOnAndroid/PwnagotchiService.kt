package com.hereliesaz.pwnagotchiOnAndroid

import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Binder
import android.os.IBinder
import android.widget.RemoteViews
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.net.URI

class PwnagotchiService : Service() {

    private val binder = LocalBinder()
    private var webSocketClient: WebSocketClient? = null
    private val _uiState = MutableStateFlow<PwnagotchiUiState>(PwnagotchiUiState.Disconnected(getString(R.string.status_not_connected)))
    val uiState: StateFlow<PwnagotchiUiState> = _uiState

    private val serviceScope = CoroutineScope(Dispatchers.IO + Job())
    private var reconnectionJob: Job? = null
    var currentUri: URI? = null
    private val maxReconnectionAttempts = 5
    private val handshakes = mutableListOf<Handshake>()
    private val plugins = mutableListOf<Plugin>()
    private val communityPlugins = mutableListOf<CommunityPlugin>()
    private var face = "(·•᷄_•᷅ ·)"
    private val json = Json { ignoreUnknownKeys = true }
    private lateinit var connectivityManager: ConnectivityManager
    private lateinit var networkCallback: ConnectivityManager.NetworkCallback
    private var isNetworkAvailable = false
    private lateinit var opwngridClient: OpwngridClient
    private lateinit var widgetStateRepository: com.hereliesaz.pwnagotchiOnAndroid.widgets.WidgetStateRepository

    inner class LocalBinder : Binder() {
        fun getService(): PwnagotchiService = this@PwnagotchiService
    }

    override fun onCreate() {
        super.onCreate()
        opwngridClient = OpwngridClient()
        widgetStateRepository = com.hereliesaz.pwnagotchiOnAndroid.widgets.WidgetStateRepository(this)
        connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        networkCallback = createNetworkCallback()
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
    }

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            "com.hereliesaz.pwnagotchiOnAndroid.RECONNECT" -> {
                val sharedPreferences = getSharedPreferences("pwnagotchi_prefs", Context.MODE_PRIVATE)
                val host = sharedPreferences.getString("host", null)
                if (host != null) {
                    connect(URI("wss://$host:8765"))
                }
            }
            else -> {
                val notification = NotificationHelper.createNotification(this, "pwnagotchi_service_channel", "Pwnagotchi Service Channel", getString(R.string.notification_service_running))
                startForeground(1, notification)
                val sharedPreferences = getSharedPreferences("pwnagotchi_prefs", Context.MODE_PRIVATE)
                val host = sharedPreferences.getString("host", null)
                if (host != null) {
                    connect(URI("wss://$host:8765"))
                }
            }
        }
        return START_STICKY
    }

    fun connect(uri: URI) {
        currentUri = uri
        reconnectionJob?.cancel() // Cancel any previous reconnection attempts
        webSocketClient?.close() // Close any existing connection

        _uiState.value = PwnagotchiUiState.Connecting(getString(R.string.status_connecting, uri.toString()))
        try {
            webSocketClient = object : WebSocketClient(uri) {
                override fun onOpen(handshakedata: ServerHandshake?) {
                    _uiState.value = PwnagotchiUiState.Connected(getString(R.string.status_connected), handshakes, plugins, face, emptyList(), communityPlugins)
                    updateNotification(getString(R.string.status_connected_to_pwnagotchi))
                    listPlugins()
                    getCommunityPlugins()
                }

                override fun onMessage(message: String?) {
                    message ?: return
                    try {
                        val baseMessage = json.decodeFromString<BaseMessage>(message)
                        when (baseMessage.type) {
                            "ui_update" -> {
                                val uiUpdate = json.decodeFromString<UiUpdateMessage>(message)
                                val data = uiUpdate.data
                                face = data.face
                                val statusText = "CH: ${data.channel} | APS: ${data.aps} | UP: ${data.uptime}"
                                val messageText = "PWND: ${data.shakes} | MODE: ${data.mode}"
                                _uiState.value = PwnagotchiUiState.Connected(statusText, handshakes, plugins, face, emptyList(), communityPlugins)
                                updateNotification(statusText)
                                serviceScope.launch {
                                    widgetStateRepository.updateFace(face)
                                    widgetStateRepository.updateMessage(messageText)
                                }
                            }
                            "handshake" -> {
                                val handshakeMsg = json.decodeFromString<HandshakeMessage>(message)
                                val data = handshakeMsg.data
                                val handshake = Handshake(
                                    ap = data.ap.hostname,
                                    sta = data.sta.mac,
                                    filename = data.filename
                                )
                                handshakes.add(handshake)
                                _uiState.value = PwnagotchiUiState.Connected(getString(R.string.status_new_handshake), handshakes, plugins, face, emptyList(), communityPlugins)
                                showHandshakeNotification(handshake)
                                serviceScope.launch {
                                    widgetStateRepository.updateHandshakes(Json.encodeToString(handshakes))
                                }
                            }
                            "plugin_list" -> {
                                val pluginListMsg = json.decodeFromString<PluginListMessage>(message)
                                plugins.clear()
                                plugins.addAll(pluginListMsg.data.map { Plugin(it.name, it.enabled) })
                                _uiState.value = PwnagotchiUiState.Connected(getString(R.string.status_plugins_loaded), handshakes, plugins, face, emptyList(), communityPlugins)
                            }
                            "community_plugin_list" -> {
                                val communityPluginListMsg = json.decodeFromString<CommunityPluginListMessage>(message)
                                communityPlugins.clear()
                                communityPlugins.addAll(communityPluginListMsg.data.map { CommunityPlugin(it.name, it.description) })
                                _uiState.value = PwnagotchiUiState.Connected(getString(R.string.status_community_plugins_loaded), handshakes, plugins, face, emptyList(), communityPlugins)
                            }
                        }
                    } catch (e: Exception) {
                        // Handle serialization exception
                        _uiState.value = PwnagotchiUiState.Error(getString(R.string.error_parsing_message, e.message))
                    }
                }

                override fun onClose(code: Int, reason: String?, remote: Boolean) {
                    _uiState.value = PwnagotchiUiState.Disconnected(getString(R.string.status_connection_closed))
                    updateNotification(getString(R.string.status_connection_closed))
                    if (isNetworkAvailable) {
                        scheduleReconnect()
                    }
                }

                override fun onError(ex: Exception?) {
                    _uiState.value = PwnagotchiUiState.Error(ex?.message ?: getString(R.string.error_unknown))
                    if (isNetworkAvailable) {
                        scheduleReconnect()
                    }
                }
            }
            webSocketClient?.connect()
        } catch (e: Exception) {
            _uiState.value = PwnagotchiUiState.Error(e.message ?: getString(R.string.error_unknown_connection))
            if (isNetworkAvailable) {
                scheduleReconnect()
            }
        }
    }

    fun disconnect() {
        currentUri = null
        reconnectionJob?.cancel()
        webSocketClient?.close()
        _uiState.value = PwnagotchiUiState.Disconnected(getString(R.string.status_disconnected_by_user))
        updateNotification(getString(R.string.status_disconnected_by_user))
    }

    fun fetchLeaderboard() {
        serviceScope.launch {
            val leaderboard = opwngridClient.getLeaderboard().mapIndexed { index, (name, pwned) -> LeaderboardEntry(name, pwned, index + 1) }
            val currentState = _uiState.value
            if (currentState is PwnagotchiUiState.Connected) {
                _uiState.value = currentState.copy(leaderboard = leaderboard)
                widgetStateRepository.updateLeaderboard(json.encodeToString(leaderboard))
            }
        }
    }

    private fun isWebSocketOpen(): Boolean {
        return webSocketClient?.isOpen == true
    }

    fun listPlugins() {
        if (isWebSocketOpen()) {
            webSocketClient?.send("{\"command\": \"list_plugins\"}")
        } else {
            _uiState.value = PwnagotchiUiState.Error(getString(R.string.error_websocket_not_open))
        }
    }

    fun togglePlugin(pluginName: String, enabled: Boolean) {
        if (isWebSocketOpen()) {
            webSocketClient?.send("{\"command\": \"toggle_plugin\", \"plugin_name\": \"$pluginName\", \"enabled\": $enabled}")
        } else {
            _uiState.value = PwnagotchiUiState.Error(getString(R.string.error_websocket_not_open))
        }
    }

    fun getCommunityPlugins() {
        if (isWebSocketOpen()) {
            webSocketClient?.send("{\"command\": \"get_community_plugins\"}")
        } else {
            _uiState.value = PwnagotchiUiState.Error(getString(R.string.error_websocket_not_open))
        }
    }

    fun installCommunityPlugin(pluginName: String) {
        if (isWebSocketOpen()) {
            webSocketClient?.send("{\"command\": \"install_community_plugin\", \"plugin_name\": \"$pluginName\"}")
        } else {
            _uiState.value = PwnagotchiUiState.Error(getString(R.string.error_websocket_not_open))
        }
    }

    private fun scheduleReconnect() {
        if (reconnectionJob?.isActive == true) return
        var attempts = 0
        reconnectionJob = serviceScope.launch {
            var delayMs = 1000L
            val maxDelayMs = 60000L
            while (attempts < maxReconnectionAttempts) {
                _uiState.value = PwnagotchiUiState.Connecting(getString(R.string.status_reconnection_attempt, attempts + 1, maxReconnectionAttempts))
                delay(delayMs)
                currentUri?.let {
                    webSocketClient?.reconnect()
                }
                delayMs = (delayMs * 2).coerceAtMost(maxDelayMs)
                attempts++
            }
            _uiState.value = PwnagotchiUiState.Error(getString(R.string.error_failed_to_reconnect, maxReconnectionAttempts))
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        webSocketClient?.close()
        connectivityManager.unregisterNetworkCallback(networkCallback)
    }

    private fun createNetworkCallback(): ConnectivityManager.NetworkCallback {
        return object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                isNetworkAvailable = true
                scheduleReconnect()
            }

            override fun onLost(network: Network) {
                isNetworkAvailable = false
                reconnectionJob?.cancel()
                _uiState.value = PwnagotchiUiState.Disconnected(getString(R.string.status_network_lost))
                updateNotification(getString(R.string.notification_network_lost))
            }
        }
    }

    private fun updateNotification(contentText: String) {
        val notification = NotificationHelper.createNotification(this, "pwnagotchi_service_channel", "Pwnagotchi Service Channel", contentText)
        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(1, notification)
    }

    private fun showHandshakeNotification(handshake: Handshake) {
        val contentText = getString(R.string.notification_handshake_captured, handshake.ap)
        val remoteViews = createRemoteViews(contentText, "New handshake!", face)
        val notification = NotificationHelper.createNotification(this, "handshake_channel", "Handshake Notifications", contentText)
        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(2, notification)
    }

    private fun createRemoteViews(contentText: String, title: String, face: String): RemoteViews {
        val remoteViews = RemoteViews(packageName, R.layout.notification_custom)
        remoteViews.setTextViewText(R.id.notification_title, title)
        remoteViews.setTextViewText(R.id.notification_message, contentText)
        remoteViews.setTextViewText(R.id.notification_face, face)
        return remoteViews
    }
}

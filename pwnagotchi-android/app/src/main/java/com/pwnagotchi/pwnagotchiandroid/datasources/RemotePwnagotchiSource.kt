package com.pwnagotchi.pwnagotchiandroid.datasources

import android.app.NotificationManager
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import com.pwnagotchi.pwnagotchiandroid.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.net.URI

class RemotePwnagotchiSource(private val context: Context) : PwnagotchiDataSource {
    private var webSocketClient: WebSocketClient? = null
    private val _uiState = MutableStateFlow<PwnagotchiUiState>(PwnagotchiUiState.Disconnected(context.getString(R.string.status_not_connected)))
    override val uiState: StateFlow<PwnagotchiUiState> get() = _uiState

    private val sourceScope = CoroutineScope(Dispatchers.IO + Job())
    private var reconnectionJob: Job? = null
    private var currentUri: URI? = null
    private val maxReconnectionAttempts = 5
    private val handshakes = mutableListOf<Handshake>()
    private val plugins = mutableListOf<Plugin>()
    private val communityPlugins = mutableListOf<CommunityPlugin>()
    private var face = "(·•᷄_•᷅ ·)"
    private val json = Json { ignoreUnknownKeys = true }
    private lateinit var connectivityManager: ConnectivityManager
    private lateinit var networkCallback: ConnectivityManager.NetworkCallback
    private var isNetworkAvailable = false
    private val widgetStateRepository = com.pwnagotchi.pwnagotchiandroid.widgets.WidgetStateRepository(context)


    init {
        setupNetworkCallback()
    }


    override suspend fun start(params: DataSourceParams?) {
        if (params is DataSourceParams.Remote) {
            connect(params.uri)
        }
    }

    private fun connect(uri: URI) {
        currentUri = uri
        reconnectionJob?.cancel()
        webSocketClient?.close()

        _uiState.value = PwnagotchiUiState.Connecting(context.getString(R.string.status_connecting, uri.toString()))
        try {
            webSocketClient = object : WebSocketClient(uri) {
                override fun onOpen(handshakedata: ServerHandshake?) {
                    _uiState.value = PwnagotchiUiState.Connected(context.getString(R.string.status_connected), handshakes, plugins, face, emptyList(), communityPlugins)
                    updateNotification(context.getString(R.string.status_connected_to_pwnagotchi))
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
                                val notificationText = "CH: ${data.channel} | APS: ${data.aps} | UP: ${data.uptime} | PWND: ${data.shakes} | MODE: ${data.mode}"
                                _uiState.value = PwnagotchiUiState.Connected(notificationText, handshakes, plugins, face, emptyList(), communityPlugins)
                                updateNotification(notificationText)
                                sourceScope.launch {
                                    widgetStateRepository.updateFace(face)
                                    widgetStateRepository.updateMessage(notificationText)
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
                                _uiState.value = PwnagotchiUiState.Connected(context.getString(R.string.status_new_handshake), handshakes, plugins, face, emptyList(), communityPlugins)
                                showHandshakeNotification(handshake)
                                sourceScope.launch {
                                    widgetStateRepository.updateHandshakes(json.encodeToString(handshakes))
                                }
                            }
                            "plugin_list" -> {
                                val pluginListMsg = json.decodeFromString<PluginListMessage>(message)
                                plugins.clear()
                                plugins.addAll(pluginListMsg.data.map { Plugin(it.name, it.enabled) })
                                _uiState.value = PwnagotchiUiState.Connected(context.getString(R.string.status_plugins_loaded), handshakes, plugins, face, emptyList(), communityPlugins)
                            }
                            "community_plugin_list" -> {
                                val communityPluginListMsg = json.decodeFromString<CommunityPluginListMessage>(message)
                                communityPlugins.clear()
                                communityPlugins.addAll(communityPluginListMsg.data.map { CommunityPlugin(it.name, it.description) })
                                _uiState.value = PwnagotchiUiState.Connected(context.getString(R.string.status_community_plugins_loaded), handshakes, plugins, face, emptyList(), communityPlugins)
                            }
                        }
                    } catch (e: Exception) {
                        _uiState.value = PwnagotchiUiState.Error(context.getString(R.string.error_parsing_message, e.message))
                    }
                }

                override fun onClose(code: Int, reason: String?, remote: Boolean) {
                    _uiState.value = PwnagotchiUiState.Disconnected(context.getString(R.string.status_connection_closed))
                    updateNotification(context.getString(R.string.status_connection_closed))
                    if (isNetworkAvailable) {
                        scheduleReconnect()
                    }
                }

                override fun onError(ex: Exception?) {
                    _uiState.value = PwnagotchiUiState.Error(ex?.message ?: context.getString(R.string.error_unknown))
                    if (isNetworkAvailable) {
                        scheduleReconnect()
                    }
                }
            }
            webSocketClient?.connect()
        } catch (e: Exception) {
            _uiState.value = PwnagotchiUiState.Error(e.message ?: context.getString(R.string.error_unknown_connection))
            if (isNetworkAvailable) {
                scheduleReconnect()
            }
        }
    }

    override suspend fun stop() {
        currentUri = null
        reconnectionJob?.cancel()
        webSocketClient?.close()
        _uiState.value = PwnagotchiUiState.Disconnected(context.getString(R.string.status_disconnected_by_user))
        updateNotification(context.getString(R.string.status_disconnected_by_user))
    }

    override suspend fun sendCommand(command: String) {
        if (webSocketClient?.isOpen == true) {
            webSocketClient?.send("{\"command\": \"$command\"}")
        } else {
            _uiState.value = PwnagotchiUiState.Error(context.getString(R.string.error_websocket_not_open))
        }
    }

    fun listPlugins() {
        sourceScope.launch { sendCommand("list_plugins") }
    }

    fun getCommunityPlugins() {
        sourceScope.launch { sendCommand("get_community_plugins") }
    }

    fun togglePlugin(pluginName: String, enabled: Boolean) {
        if (webSocketClient?.isOpen == true) {
            webSocketClient?.send("{\"command\": \"toggle_plugin\", \"plugin_name\": \"$pluginName\", \"enabled\": $enabled}")
        } else {
            _uiState.value = PwnagotchiUiState.Error(context.getString(R.string.error_websocket_not_open))
        }
    }

    fun installCommunityPlugin(pluginName: String) {
        if (webSocketClient?.isOpen == true) {
            webSocketClient?.send("{\"command\": \"install_community_plugin\", \"plugin_name\": \"$pluginName\"}")
        } else {
            _uiState.value = PwnagotchiUiState.Error(context.getString(R.string.error_websocket_not_open))
        }
    }

    private fun scheduleReconnect() {
        if (reconnectionJob?.isActive == true) return
        var attempts = 0
        reconnectionJob = sourceScope.launch {
            var delayMs = 1000L
            val maxDelayMs = 60000L
            while (attempts < maxReconnectionAttempts) {
                _uiState.value = PwnagotchiUiState.Connecting(context.getString(R.string.status_reconnection_attempt, attempts + 1, maxReconnectionAttempts))
                delay(delayMs)
                currentUri?.let {
                    webSocketClient?.reconnect()
                }
                delayMs = (delayMs * 2).coerceAtMost(maxDelayMs)
                attempts++
            }
            _uiState.value = PwnagotchiUiState.Error(context.getString(R.string.error_failed_to_reconnect, maxReconnectionAttempts))
        }
    }

    private fun setupNetworkCallback() {
        connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                isNetworkAvailable = true
                scheduleReconnect()
            }

            override fun onLost(network: Network) {
                isNetworkAvailable = false
                reconnectionJob?.cancel()
                _uiState.value = PwnagotchiUiState.Disconnected(context.getString(R.string.status_network_lost))
                updateNotification(context.getString(R.string.notification_network_lost))
            }
        }
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
    }

    private fun updateNotification(contentText: String) {
        val notification = NotificationHelper.createNotification(context, "pwnagotchi_service_channel", "Pwnagotchi Service Channel", contentText)
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(1, notification)
    }

    private fun showHandshakeNotification(handshake: Handshake) {
        val contentText = context.getString(R.string.notification_handshake_captured, handshake.ap)
        val notification = NotificationHelper.createNotification(context, "handshake_channel", "Handshake Notifications", contentText, NotificationManager.IMPORTANCE_HIGH)
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(2, notification)
    }
}
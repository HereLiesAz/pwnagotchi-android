package com.pwnagotchi.pwnagotchiOnAndroid

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import com.pwnagotchi.pwnagotchiOnAndroid.datasources.LocalPwnagotchiSource
import com.pwnagotchi.pwnagotchiOnAndroid.datasources.PwnagotchiDataSource
import com.pwnagotchi.pwnagotchiOnAndroid.datasources.RemotePwnagotchiSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.net.URI

class PwnagotchiService : Service() {
    private val binder = LocalBinder()
    private val serviceScope = CoroutineScope(Dispatchers.IO + Job())
    private var stateJob: Job? = null

    private val _uiState = MutableStateFlow<PwnagotchiUiState>(PwnagotchiUiState.Disconnected("Select a mode"))
    val uiState: StateFlow<PwnagotchiUiState> = _uiState

    private val localSource by lazy { LocalPwnagotchiSource(applicationContext) }
    private val remoteSource by lazy { RemotePwnagotchiSource(applicationContext) }
    private var activeDataSource: PwnagotchiDataSource? = null

    inner class LocalBinder : Binder() {
        fun getService(): PwnagotchiService = this@PwnagotchiService
    }

    override fun onBind(intent: Intent): IBinder = binder

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = NotificationHelper.createNotification(this, "pwnagotchi_service_channel", "Pwnagotchi Service Channel", "Pwnagotchi Service is running")
        startForeground(1, notification)
        return START_STICKY
    }

    fun setMode(mode: AppMode) {
        stateJob?.cancel()
        activeDataSource = when (mode) {
            AppMode.LOCAL -> localSource
            AppMode.REMOTE -> remoteSource
        }
        stateJob = serviceScope.launch {
            activeDataSource?.uiState?.collect { _uiState.value = it }
        }
    }

    fun connect(uri: URI) {
        if (activeDataSource is RemotePwnagotchiSource) {
            serviceScope.launch { activeDataSource?.start(mapOf("uri" to uri)) }
        }
    }

    fun startLocalAgent() {
        if (activeDataSource is LocalPwnagotchiSource) {
            serviceScope.launch { activeDataSource?.start() }
        }
    }

    fun disconnect() {
        serviceScope.launch { activeDataSource?.stop() }
    }

    fun sendCommand(command: String) {
        serviceScope.launch { activeDataSource?.sendCommand(command) }
    }


    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
}

enum class AppMode {
    LOCAL, REMOTE
}

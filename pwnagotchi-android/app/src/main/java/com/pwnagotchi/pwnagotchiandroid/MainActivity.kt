package com.pwnagotchi.pwnagotchiandroid

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.pwnagotchi.pwnagotchiandroid.ui.MainScreen
import com.pwnagotchi.pwnagotchiandroid.ui.theme.PwnagotchiAndroidTheme
import java.net.URI

class MainActivity : ComponentActivity() {
    private var pwnagotchiService: PwnagotchiService? = null
    private var isBound = false
    private val pwnagotchiViewModel: PwnagotchiViewModel by viewModels()

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as PwnagotchiService.LocalBinder
            pwnagotchiService = binder.getService()
            pwnagotchiService?.let {
                pwnagotchiViewModel.setService(it)
            }
            isBound = true
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            isBound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Intent(this, PwnagotchiService::class.java).also { intent ->
            startService(intent)
            bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }

        setContent {
            val pwnagotchiUiState by pwnagotchiViewModel.uiState.collectAsState()

            PwnagotchiAndroidTheme {
                MainScreen(
                    pwnagotchiUiState = pwnagotchiUiState,
                    onConnect = { host -> pwnagotchiService?.connect(URI("wss://$host:8765")) },
                    onDisconnect = { pwnagotchiService?.disconnect() },
                    onTogglePlugin = { plugin, enabled -> pwnagotchiService?.togglePlugin(plugin, enabled) },
                    onInstallPlugin = { plugin -> pwnagotchiService?.installCommunityPlugin(plugin) },
                    onSaveSettings = { host, _, _ -> pwnagotchiService?.connect(URI("wss://$host:8765")) }
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isBound) {
            unbindService(connection)
            isBound = false
        }
    }
}
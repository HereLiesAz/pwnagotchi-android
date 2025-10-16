package com.pwnagotchi.pwnagotchiandroid

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pwnagotchi.pwnagotchiandroid.ui.MainScreen
import com.pwnagotchi.pwnagotchiandroid.ui.theme.PwnagotchiAndroidTheme

class MainActivity : ComponentActivity() {
    private var pwnagotchiService: PwnagotchiService? = null
    private var isBound = false

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as PwnagotchiService.PwnagotchiBinder
            pwnagotchiService = binder.getService()
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
            val pwnagotchiViewModel: PwnagotchiViewModel = viewModel()
            pwnagotchiService?.let {
                pwnagotchiViewModel.setService(it)
            }
            val pwnagotchiUiState by pwnagotchiViewModel.uiState.collectAsState()

            PwnagotchiAndroidTheme {
                MainScreen(
                    pwnagotchiUiState = pwnagotchiUiState,
                    onConnect = { host -> pwnagotchiService?.connect(host) },
                    onDisconnect = { pwnagotchiService?.disconnect() },
                    onTogglePlugin = { plugin, enabled -> pwnagotchiService?.togglePlugin(plugin, enabled) },
                    onInstallPlugin = { plugin -> pwnagotchiService?.installPlugin(plugin) },
                    onSaveSettings = { host, _ -> pwnagotchiService?.reconnect(host) }
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
package com.pwnagotchi.pwnagotchiandroid

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.pwnagotchi.pwnagotchiandroid.core.Constants
import com.pwnagotchi.pwnagotchiandroid.ui.theme.PwnagotchiAndroidTheme

class MainActivity : ComponentActivity() {
    private val pwnagotchiViewModel: PwnagotchiViewModel by viewModels()
    private var pwnagotchiService: PwnagotchiService? = null
    private var isServiceBound = false

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as PwnagotchiService.LocalBinder
            pwnagotchiService = binder.getService()
            isServiceBound = true
            pwnagotchiViewModel.setService(pwnagotchiService)
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            isServiceBound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val serviceIntent = Intent(this, PwnagotchiService::class.java)
        startService(serviceIntent)
        bindService(serviceIntent, connection, Context.BIND_AUTO_CREATE)

        setContent {
            PwnagotchiAndroidTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NavGraph(
                        mainViewModel = pwnagotchiViewModel,
                        onTogglePlugin = { pluginName, enabled ->
                            pwnagotchiService?.togglePlugin(pluginName, enabled)
                        },
                        onInstallPlugin = { pluginName ->
                            pwnagotchiService?.installCommunityPlugin(pluginName)
                        },
                        onSaveSettings = { host ->
                            val sharedPreferences = getSharedPreferences("pwnagotchi_prefs", Context.MODE_PRIVATE)
                            sharedPreferences.edit()
                                .putString("host", host)
                                .apply()
                            pwnagotchiService?.disconnect()
                            pwnagotchiService?.connect(java.net.URI("wss://$host:${Constants.WEBSOCKET_PORT}"))
                        },
                        onReconnect = {
                            pwnagotchiService?.disconnect()
                            val sharedPreferences = getSharedPreferences("pwnagotchi_prefs", Context.MODE_PRIVATE)
                            val host = sharedPreferences.getString("host", "127.0.0.1") ?: "127.0.0.1"
                            pwnagotchiService?.connect(java.net.URI("wss://$host:${Constants.WEBSOCKET_PORT}"))
                        }
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isServiceBound) {
            unbindService(connection)
            isServiceBound = false
        }
    }
}

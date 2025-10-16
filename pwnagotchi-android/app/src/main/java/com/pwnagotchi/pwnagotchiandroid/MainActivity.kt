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

/**
 * The main and only activity of the application.
 *
 * This activity is responsible for:
 * - Launching and binding to the `PwnagotchiService`.
 * - Hosting the Jetpack Compose UI.
 * - Providing the `PwnagotchiViewModel` to the UI layer.
 *
 * FUTURE ARCHITECTURE:
 * This Activity's `setContent` block will be significantly refactored. It will host the
 * `MainScreen` composable, which will contain the new `AzNavRail` and `NavHost` for
 * managing the application's navigation. The current system of passing many individual
 * lambdas to the `MainScreen` will be replaced by passing the `PwnagotchiViewModel`
 * and a `NavController` instance, creating a cleaner and more scalable UI architecture.
 */
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
                    onDisconnect = { pwnagotchiService?.disconnect() },
                    onTogglePlugin = { plugin, enabled -> pwnagotchiService?.togglePlugin(plugin, enabled) },
                    onInstallPlugin = { plugin -> pwnagotchiService?.installCommunityPlugin(plugin) },
                    onSaveSettings = { host, _, _ -> pwnagotchiService?.connect(URI("wss://$host:8765")) },
                    onReconnect = {
                        val sharedPreferences = getSharedPreferences("settings", Context.MODE_PRIVATE)
                        val host = sharedPreferences.getString("host", "10.0.0.2") ?: "10.0.0.2"
                        pwnagotchiService?.connect(URI("wss://$host:8765"))
                    }
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
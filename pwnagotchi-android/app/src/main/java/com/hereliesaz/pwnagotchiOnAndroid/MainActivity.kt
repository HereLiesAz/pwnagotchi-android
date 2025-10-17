package com.hereliesaz.pwnagotchiOnAndroid

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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.hereliesaz.pwnagotchiOnAndroid.ui.MainScreen
import com.hereliesaz.pwnagotchiOnAndroid.ui.theme.PwnagotchiOnAndroidTheme
import com.hereliesaz.pwnagotchiOnAndroid.ui.screens.OnboardingScreen
import androidx.core.content.edit
import java.net.URI

class MainActivity : ComponentActivity() {
    private var pwnagotchiService: PwnagotchiService? = null
    private var isBound = false
    private val pwnagotchiViewModel: PwnagotchiViewModel by viewModels()
    private var pwnagotchiService: PwnagotchiService? = null

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
            bindService(intent, connection, BIND_AUTO_CREATE)
        }

        setContent {
            val sharedPreferences = getSharedPreferences("pwnagotchi_prefs", Context.MODE_PRIVATE)
            var showOnboarding by remember { mutableStateOf(!sharedPreferences.getBoolean("onboarding_complete", false)) }
            val pwnagotchiUiState by pwnagotchiViewModel.uiState.collectAsState()

            PwnagotchiOnAndroidTheme {
                if (showOnboarding) {
                    OnboardingScreen {
                        sharedPreferences.edit { putBoolean("onboarding_complete", true) }
                        showOnboarding = false
                    }
                } else {
                    MainScreen(
                        pwnagotchiUiState = pwnagotchiUiState,
                        onDisconnect = { pwnagotchiService?.disconnect() },
                        onTogglePlugin = { plugin, enabled -> pwnagotchiService?.togglePlugin(plugin, enabled) },
                        onInstallPlugin = { plugin -> pwnagotchiService?.installCommunityPlugin(plugin) },
                        onSaveSettings = { host, apiKey, city ->
                            val editor = sharedPreferences.edit()
                            editor.putString("host", host)
                            editor.putString("api_key", apiKey)
                            editor.putString("city", city)
                            editor.apply()
                        },
                        onReconnect = { pwnagotchiService?.connect(pwnagotchiService?.currentUri!!) }
                    )
                }
            PwnagotchiOnAndroidTheme {  }AndroidTheme {
                MainScreen(
                    pwnagotchiUiState = pwnagotchiUiState,
                    onDisconnect = { pwnagotchiService?.disconnect() },
                    onTogglePlugin = { plugin, enabled -> pwnagotchiService?.togglePlugin(plugin, enabled) },
                    onInstallPlugin = { plugin -> pwnagotchiService?.installCommunityPlugin(plugin) },
                    onSaveSettings = { host, _, _ -> pwnagotchiService?.connect(URI("wss://$host:8765")) },
                    onReconnect = {
                        val sharedPreferences = getSharedPreferences("settings", MODE_PRIVATE)
                        val host = sharedPreferences.getString("host", "10.0.0.2") ?: "10.0.0.2"
                        pwnagotchiService?.connect(URI("wss://$host:8765"))
                if (showOnboarding) {
                    OnboardingScreen {
                        sharedPreferences.edit().putBoolean("onboarding_complete", true).apply()
                        showOnboarding = false
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

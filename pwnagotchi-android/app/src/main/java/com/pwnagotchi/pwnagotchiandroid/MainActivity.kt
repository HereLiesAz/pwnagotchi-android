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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.pwnagotchi.pwnagotchiandroid.ui.MainScreen
import com.pwnagotchi.pwnagotchiandroid.ui.screens.OnboardingScreen
import com.pwnagotchi.pwnagotchiandroid.ui.theme.PwnagotchiAndroidTheme

class MainActivity : ComponentActivity() {
    private val pwnagotchiViewModel: PwnagotchiViewModel by viewModels()

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as PwnagotchiService.LocalBinder
            val pwnagotchiService = binder.getService()
            pwnagotchiViewModel.setService(pwnagotchiService)
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            pwnagotchiViewModel.setService(null)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Intent(this, PwnagotchiService::class.java).also { intent ->
            startService(intent)
            bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }

        setContent {
            val sharedPreferences = getSharedPreferences("pwnagotchi_prefs", Context.MODE_PRIVATE)
            var showOnboarding by remember { mutableStateOf(!sharedPreferences.getBoolean("onboarding_complete", false)) }

            PwnagotchiAndroidTheme {
                if (showOnboarding) {
                    OnboardingScreen {
                        sharedPreferences.edit().putBoolean("onboarding_complete", true).apply()
                        showOnboarding = false
                    }
                } else {
                    MainScreen(viewModel = pwnagotchiViewModel)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindService(connection)
    }
}
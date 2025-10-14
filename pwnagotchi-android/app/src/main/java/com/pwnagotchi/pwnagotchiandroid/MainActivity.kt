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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.pwnagotchi.pwnagotchiandroid.ui.theme.PwnagotchiAndroidTheme

// TODO: Refactor navigation to use Jetpack Navigation for better readability and maintainability.
// This is a significant architectural change that will be addressed in a future task.
class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()
    private var bettercapService: BettercapService? = null
    private var isServiceBound = false

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as BettercapService.LocalBinder
            bettercapService = binder.getService()
            isServiceBound = true
            viewModel.setService(bettercapService!!)
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            isServiceBound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PwnagotchiAndroidTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var showSetup by remember { mutableStateOf(false) }
                    var showMain by remember { mutableState of(false) }

                    when {
                        showMain -> MainScreen(viewModel)
                        showSetup -> SetupScreen(onSetupComplete = {
                            val intent = Intent(this, BettercapService::class.java)
                            startService(intent)
                            bindService(intent, connection, Context.BIND_AUTO_CREATE)
                            showMain = true
                        })
                        else -> CompatibilityScreen(onNavigateToSetup = { showSetup = true })
                    }
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

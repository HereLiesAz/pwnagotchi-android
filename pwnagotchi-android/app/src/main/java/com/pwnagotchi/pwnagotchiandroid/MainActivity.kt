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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pwnagotchi.pwnagotchiandroid.ui.theme.PwnagotchiAndroidTheme
import com.topjohnwu.superuser.Shell
import java.net.URI

class MainActivity : ComponentActivity() {
    private val viewModel: PwnagotchiViewModel by viewModels()
    private var pwnagotchiService: PwnagotchiService? = null
    private var isServiceBound = false

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as PwnagotchiService.LocalBinder
            pwnagotchiService = binder.getService()
            isServiceBound = true
            viewModel.setService(pwnagotchiService)
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
                    val uiState by viewModel.uiState.collectAsState()
                    var rootStatus by remember { mutableStateOf("Root status: Unknown") }
                    PwnagotchiScreen(
                        uiState = uiState,
                        rootStatus = rootStatus,
                        onConnect = { ipAddress ->
                            pwnagotchiService?.connect(URI("ws://$ipAddress:8765"))
                        },
                        onDisconnect = {
                            pwnagotchiService?.disconnect()
                        },
                        onRequestRoot = {
                            Shell.getShell { shell ->
                                rootStatus = if (shell.isRoot) {
                                    "Root status: Granted"
                                } else {
                                    "Root status: Denied"
                                }
                            }
                        }
                    )
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        Intent(this, PwnagotchiService::class.java).also { intent ->
            bindService(intent, connection, Context.BIND_AUTO_CREATE)
            startService(intent)
        }
    }

    override fun onStop() {
        super.onStop()
        unbindService(connection)
        isServiceBound = false
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PwnagotchiScreen(
    uiState: PwnagotchiUiState,
    rootStatus: String,
    onConnect: (String) -> Unit,
    onDisconnect: () -> Unit,
    onRequestRoot: () -> Unit,
    modifier: Modifier = Modifier
) {
    var ipAddress by remember { mutableStateOf("192.168.1.100") }
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when (uiState) {
            is PwnagotchiUiState.Connecting -> {
                CircularProgressIndicator()
                Text(text = uiState.message)
            }
            is PwnagotchiUiState.Connected -> Text(text = uiState.data)
            is PwnagotchiUiState.Disconnected -> Text(text = uiState.reason)
            is PwnagotchiUiState.Error -> Text(text = uiState.message)
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = ipAddress,
                onValueChange = { ipAddress = it },
                label = { Text("Pwnagotchi IP") },
                modifier = Modifier.weight(1f)
            )
            Button(onClick = { onConnect(ipAddress) }) {
                Text("Connect")
            }
        }
        Button(onClick = onDisconnect) {
            Text("Disconnect")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = rootStatus)
        Button(onClick = onRequestRoot) {
            Text("Request Root")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PwnagotchiScreenPreview() {
    PwnagotchiAndroidTheme {
        PwnagotchiScreen(
            uiState = PwnagotchiUiState.Connected("Preview"),
            rootStatus = "Root status: Unknown",
            onConnect = {},
            onDisconnect = {},
            onRequestRoot = {}
        )
    }
}

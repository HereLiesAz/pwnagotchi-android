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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
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
                    var showSettings by remember { mutableStateOf(false) }
                    var showPlugins by remember { mutableStateOf(false) }
                    when {
                        showSettings -> {
                            SettingsScreen(
                                onSave = { ipAddress ->
                                    pwnagotchiService?.connect(URI("ws://$ipAddress:8765"))
                                    showSettings = false
                                },
                                onBack = { showSettings = false }
                            )
                        }
                        showPlugins -> {
                            val uiState by viewModel.uiState.collectAsState()
                            if (uiState is PwnagotchiUiState.Connected) {
                                PluginsScreen(
                                    plugins = (uiState as PwnagotchiUiState.Connected).plugins,
                                    onTogglePlugin = { pluginName, enabled ->
                                        pwnagotchiService?.togglePlugin(pluginName, enabled)
                                    },
                                    onBack = { showPlugins = false }
                                )
                            }
                        }
                        else -> {
                            val uiState by viewModel.uiState.collectAsState()
                            var rootStatus by remember { mutableStateOf(getString(R.string.root_status_unknown)) }
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
                                            getString(R.string.root_status_granted)
                                        } else {
                                            getString(R.string.root_status_denied)
                                        }
                                    }
                                },
                                onSettings = { showSettings = true },
                                onPlugins = { showPlugins = true }
                            )
                        }
                    }
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
    onSettings: () -> Unit,
    onPlugins: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val sharedPreferences = remember {
        context.getSharedPreferences("pwnagotchi_prefs", Context.MODE_PRIVATE)
    }
    var ipAddress by remember {
        mutableStateOf(sharedPreferences.getString("ip_address", "192.168.1.100") ?: "192.168.1.100")
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.app_name)) },
                actions = {
                    IconButton(onClick = onSettings) {
                        Icon(Icons.Filled.Settings, contentDescription = stringResource(id = R.string.settings))
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ConnectionStatus(uiState = uiState)
            Spacer(modifier = Modifier.height(16.dp))
            ConnectionControls(
                ipAddress = ipAddress,
                onIpAddressChange = { ipAddress = it },
                onConnect = { onConnect(ipAddress) },
                onDisconnect = onDisconnect
            )
            Button(onClick = onPlugins) {
                Text(stringResource(id = R.string.plugins))
            }
            Spacer(modifier = Modifier.height(16.dp))
            RootControls(rootStatus = rootStatus, onRequestRoot = onRequestRoot)
            Spacer(modifier = Modifier.height(16.dp))
            HandshakeList(uiState = uiState)
        }
    }
}

@Composable
fun ConnectionStatus(uiState: PwnagotchiUiState) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (uiState) {
                is PwnagotchiUiState.Connecting -> {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = uiState.message, style = MaterialTheme.typography.bodyMedium)
                }
                is PwnagotchiUiState.Connected -> {
                    Text(text = uiState.face, style = MaterialTheme.typography.displayLarge)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = stringResource(id = R.string.connected), style = MaterialTheme.typography.titleMedium)
                    Text(text = uiState.data, style = MaterialTheme.typography.bodyMedium)
                }
                is PwnagotchiUiState.Disconnected -> {
                    Text(text = stringResource(id = R.string.disconnected), style = MaterialTheme.typography.titleMedium)
                    Text(text = uiState.reason, style = MaterialTheme.typography.bodyMedium)
                }
                is PwnagotchiUiState.Error -> {
                    Text(text = stringResource(id = R.string.error), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.error)
                    Text(text = uiState.message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConnectionControls(
    ipAddress: String,
    onIpAddressChange: (String) -> Unit,
    onConnect: () -> Unit,
    onDisconnect: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedTextField(
            value = ipAddress,
            onValueChange = onIpAddressChange,
            label = { Text(stringResource(id = R.string.pwnagotchi_ip)) },
            modifier = Modifier.weight(1f)
        )
        Button(onClick = onConnect) {
            Text(stringResource(id = R.string.connect))
        }
        Button(onClick = onDisconnect) {
            Text(stringResource(id = R.string.disconnect))
        }
    }
}

@Composable
fun RootControls(rootStatus: String, onRequestRoot: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(text = rootStatus, modifier = Modifier.weight(1f))
        Button(onClick = onRequestRoot) {
            Text(stringResource(id = R.string.request_root))
        }
    }
}

@Composable
fun HandshakeList(uiState: PwnagotchiUiState) {
    if (uiState is PwnagotchiUiState.Connected && uiState.handshakes.isNotEmpty()) {
        LazyColumn {
            items(uiState.handshakes) { handshake ->
                HandshakeItem(handshake = handshake)
            }
        }
    }
}

@Composable
fun HandshakeItem(handshake: Handshake) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(text = "${stringResource(id = R.string.ap)}: ${handshake.ap}", style = MaterialTheme.typography.bodyLarge)
            Text(text = "${stringResource(id = R.string.sta)}: ${handshake.sta}", style = MaterialTheme.typography.bodyMedium)
            Text(text = "${stringResource(id = R.string.file)}: ${handshake.filename}", style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PwnagotchiScreenPreview() {
    PwnagotchiAndroidTheme {
        PwnagotchiScreen(
            uiState = PwnagotchiUiState.Connected(
                "Preview",
                listOf(
                    Handshake("AP1", "STA1", "file1.pcap"),
                    Handshake("AP2", "STA2", "file2.pcap")
                ),
                face = "(^-^)"
            ),
            rootStatus = "Root status: Unknown",
            onConnect = {},
            onDisconnect = {},
            onRequestRoot = {},
            onSettings = {},
            onPlugins = {}
        )
    }
}

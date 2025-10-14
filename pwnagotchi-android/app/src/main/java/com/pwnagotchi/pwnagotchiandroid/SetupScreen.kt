package com.pwnagotchi.pwnagotchiandroid

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.topjohnwu.superuser.Shell

@Composable
fun SetupScreen(onSetupComplete: () -> Unit) {
    val context = LocalContext.current
    var bettercapInstalled by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Setup",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text("This screen will guide you through the process of setting up your device to run as a Pwnagotchi.")
        Spacer(modifier = Modifier.height(16.dp))
        if (!bettercapInstalled) {
            Text("Step 1: Install bettercap")
            Text("You will need to install bettercap for your device's architecture. You can find instructions on the bettercap website.")
            Button(onClick = {
                Shell.cmd("which bettercap").to(mutableListOf(), mutableListOf()).exec { result ->
                    bettercapInstalled = result.isSuccess
                }
            }) {
                Text("Check for bettercap")
            }
        } else {
            Text("bettercap is installed!")
            Spacer(modifier = Modifier.height(16.dp))
            Text("Step 2: Enable monitor mode")
            Text("The following commands will be run to enable monitor mode:")
            Text("ip link set wlan0 down")
            Text("echo 4 > /sys/module/wlan/parameters/con_mode")
            Text("ip link set wlan0 up")
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = {
                Shell.cmd("ip link set wlan0 down", "echo 4 > /sys/module/wlan/parameters/con_mode", "ip link set wlan0 up").exec()
                val intent = Intent(context, BettercapService::class.java)
                context.startService(intent)
                onSetupComplete()
            }) {
                Text("Run Setup")
            }
        }
    }
}

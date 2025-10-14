package com.pwnagotchi.pwnagotchiandroid

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.pwnagotchi.pwnagotchiandroid.utils.NetworkUtils
import com.topjohnwu.superuser.Shell

@Composable
fun SetupScreen(onSetupComplete: () -> Unit) {
    val context = LocalContext.current
    val sharedPreferences = remember {
        context.getSharedPreferences("pwnagotchi_prefs", Context.MODE_PRIVATE)
    }
    val unsafeInterfaceName = sharedPreferences.getString("interface_name", NetworkUtils.getWifiInterfaceName()) ?: NetworkUtils.getWifiInterfaceName()
    val interfaceName = unsafeInterfaceName.filter { it.isLetterOrDigit() }
    var selectedMethod by remember { mutableStateOf("qualcomm") }
    var setupError by remember { mutableStateOf<String?>(null) }
    var bettercapInstalled by remember { mutableStateOf<Boolean?>(null) }


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
        when (bettercapInstalled) {
            null -> {
                Text("Step 1: Check for bettercap")
                Button(onClick = {
                    // First check if bettercap is in PATH
                    Shell.cmd("which bettercap").to(mutableListOf(), mutableListOf()).exec { whichResult ->
                        if (!whichResult.isSuccess) {
                            bettercapInstalled = false
                            setupError = "bettercap not found in PATH. Please install it and make sure it's in your PATH."
                        } else {
                            // If found, check if it is executable and get version
                            Shell.cmd("bettercap -version").to(mutableListOf(), mutableListOf()).exec { versionResult ->
                                bettercapInstalled = versionResult.isSuccess
                                if (!versionResult.isSuccess) {
                                    setupError = "bettercap found but not executable or version check failed. Please ensure it is installed correctly."
                                }
                            }
                        }
                    }
                }) {
                    Text("Check for bettercap")
                }
            }
            true -> {
                Text("bettercap is installed!")
                Spacer(modifier = Modifier.height(16.dp))
                Text("Step 2: Select Monitor Mode Method")
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = selectedMethod == "qualcomm", onClick = { selectedMethod = "qualcomm" })
                    Text("Qualcomm")
                    RadioButton(selected = selectedMethod == "nexmon", onClick = { selectedMethod = "nexmon" })
                    Text("Nexmon")
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = {
                    val command = when (selectedMethod) {
                        "qualcomm" -> "echo 4 > /sys/module/wlan/parameters/con_mode"
                        "nexmon" -> "nexutil -m2"
                        else -> ""
                    }
                    if (command.isNotEmpty()) {
                        Shell.cmd("ip link set $interfaceName down", command, "ip link set $interfaceName up").to(mutableListOf(), mutableListOf()).exec { result ->
                            if (result.isSuccess) {
                                val intent = Intent(context, BettercapService::class.java)
                                context.startService(intent)
                                onSetupComplete()
                            } else {
                                setupError = "Failed to enable monitor mode. Your device may not be supported."
                            }
                        }
                    } else {
                        setupError = "Please select a monitor mode method."
                    }
                }) {
                    Text("Run Setup")
                }
            }
            false -> {
                Text("bettercap not found. Please install it and make sure it's in your PATH.")
            }
        }
        setupError?.let {
            Spacer(modifier = Modifier.height(16.dp))
            Text(it, color = MaterialTheme.colorScheme.error)
        }
    }
}

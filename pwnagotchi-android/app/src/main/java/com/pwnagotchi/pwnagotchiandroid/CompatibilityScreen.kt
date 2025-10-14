package com.pwnagotchi.pwnagotchiandroid

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompatibilityScreen(onNavigateToSetup: () -> Unit) {
    var rootStatus by remember { mutableStateOf<Boolean?>(null) }
    val context = LocalContext.current
    val sharedPreferences = remember {
        context.getSharedPreferences("pwnagotchi_prefs", Context.MODE_PRIVATE)
    }
    var interfaceName by remember {
        mutableStateOf(sharedPreferences.getString("interface_name", "wlan0") ?: "wlan0")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Device Compatibility",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(16.dp))
        when (rootStatus) {
            null -> Text("Checking for root access...")
            true -> Text("Root access granted!")
            false -> Text("Root access not granted.")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            Shell.getShell { shell ->
                rootStatus = shell.isRoot
            }
        }) {
            Text(stringResource(id = R.string.check_root_access))
        }
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = interfaceName,
            onValueChange = { interfaceName = it },
            label = { Text("Wireless Interface Name") },
            modifier = Modifier.fillMaxWidth()
        )
        if (rootStatus == true) {
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = {
                sharedPreferences.edit().putString("interface_name", interfaceName).apply()
                onNavigateToSetup()
            }) {
                Text("Continue to Setup")
            }
        }
    }
}

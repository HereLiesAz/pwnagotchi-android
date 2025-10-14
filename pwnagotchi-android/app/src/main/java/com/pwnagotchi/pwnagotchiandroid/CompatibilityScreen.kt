package com.pwnagotchi.pwnagotchiandroid

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.topjohnwu.superuser.Shell

@Composable
fun CompatibilityScreen(onNavigateToSetup: () -> Unit) {
    var rootStatus by remember { mutableStateOf<Boolean?>(null) }

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
        if (rootStatus == true) {
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onNavigateToSetup) {
                Text("Continue to Setup")
            }
        }
    }
}

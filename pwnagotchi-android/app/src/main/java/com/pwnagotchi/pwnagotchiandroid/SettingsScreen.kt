package com.pwnagotchi.pwnagotchiandroid

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onSave: (String) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val sharedPreferences = remember {
        context.getSharedPreferences("pwnagotchi_prefs", Context.MODE_PRIVATE)
    }
    var ipAddress by remember {
        mutableStateOf(sharedPreferences.getString("ip_address", "192.168.1.100") ?: "192.168.1.100")
    }
    var theme by remember {
        mutableStateOf(sharedPreferences.getString("theme", "System") ?: "System")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(
            value = ipAddress,
            onValueChange = { ipAddress = it },
            label = { Text(stringResource(id = R.string.pwnagotchi_ip)) },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(stringResource(id = R.string.theme), style = androidx.compose.material3.MaterialTheme.typography.titleMedium)
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(selected = theme == "System", onClick = { theme = "System" })
            Text(stringResource(id = R.string.system))
            RadioButton(selected = theme == "Light", onClick = { theme = "Light" })
            Text(stringResource(id = R.string.light))
            RadioButton(selected = theme == "Dark", onClick = { theme = "Dark" })
            Text(stringResource(id = R.string.dark))
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            sharedPreferences.edit()
                .putString("ip_address", ipAddress)
                .putString("theme", theme)
                .apply()
            onSave(ipAddress)
        }) {
            Text(stringResource(id = R.string.save))
        }
        Button(onClick = onBack) {
            Text(stringResource(id = R.string.back))
        }
    }
}

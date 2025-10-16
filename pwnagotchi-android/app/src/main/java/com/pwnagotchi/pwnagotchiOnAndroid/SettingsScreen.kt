package com.pwnagotchi.pwnagotchiOnAndroid

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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.pwnagotchi.pwnagotchiOnAndroid.viewmodels.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel
) {
    val host by viewModel.host.collectAsState()
    val apiKey by viewModel.apiKey.collectAsState()
    val theme by viewModel.theme.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(
            value = host,
            onValueChange = { viewModel.onHostChanged(it) },
            label = { Text(stringResource(id = R.string.websocket_host)) },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = apiKey,
            onValueChange = { viewModel.onApiKeyChanged(it) },
            label = { Text(stringResource(id = R.string.opwngrid_api_key)) },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(stringResource(id = R.string.theme), style = androidx.compose.material3.MaterialTheme.typography.titleMedium)
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(selected = theme == "System", onClick = { viewModel.onThemeChanged("System") })
            Text(stringResource(id = R.string.system))
            RadioButton(selected = theme == "Light", onClick = { viewModel.onThemeChanged("Light") })
            Text(stringResource(id = R.string.light))
            RadioButton(selected = theme == "Dark", onClick = { viewModel.onThemeChanged("Dark") })
            Text(stringResource(id = R.string.dark))
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            // TODO: Implement custom theme loading
        }) {
            Text(stringResource(id = R.string.select_custom_theme))
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            viewModel.saveSettings()
        }) {
            Text(stringResource(id = R.string.save))
        }
    }
}

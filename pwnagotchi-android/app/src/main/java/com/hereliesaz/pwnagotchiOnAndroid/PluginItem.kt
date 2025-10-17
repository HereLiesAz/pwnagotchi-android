package com.hereliesaz.pwnagotchiOnAndroid

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun PluginItem(plugin: Plugin, onToggle: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = plugin.name, modifier = Modifier.weight(1f))
        Switch(checked = plugin.enabled, onCheckedChange = onToggle)
    }
}

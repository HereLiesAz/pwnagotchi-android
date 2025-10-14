package com.pwnagotchi.pwnagotchiandroid

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable

@Composable
fun InstalledPluginsScreen(
    plugins: List<Plugin>,
    onTogglePlugin: (String, Boolean) -> Unit
) {
    LazyColumn {
        items(plugins) { plugin ->
            PluginItem(plugin = plugin, onToggle = { onTogglePlugin(plugin.name, it) })
        }
    }
}

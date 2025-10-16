package com.pwnagotchi.pwnagotchiandroid.ui

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.pwnagotchi.pwnagotchiandroid.SettingsScreen
import com.pwnagotchi.pwnagotchiandroid.core.Constants

@Composable
fun SettingsScreenNav(
    onSave: (String, String) -> Unit
) {
    val context = LocalContext.current
    val sharedPreferences = context.getSharedPreferences("pwnagotchi_prefs", Context.MODE_PRIVATE)
    val host = sharedPreferences.getString("host", Constants.DEFAULT_PWNAGOTCHI_IP) ?: Constants.DEFAULT_PWNAGOTCHI_IP
    val apiKey = sharedPreferences.getString("opwngrid_api_key", "") ?: ""

    SettingsScreen(
        host = host,
        apiKey = apiKey,
        onSave = { newHost, newApiKey ->
            sharedPreferences.edit()
                .putString("host", newHost)
                .putString("opwngrid_api_key", newApiKey)
                .apply()
            onSave(newHost, newApiKey)
        }
    )
}
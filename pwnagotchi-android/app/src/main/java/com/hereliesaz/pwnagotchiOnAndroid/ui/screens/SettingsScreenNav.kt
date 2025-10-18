package com.hereliesaz.pwnagotchiOnAndroid.ui.screens

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.hereliesaz.pwnagotchiOnAndroid.SettingsScreen

@Composable
fun SettingsScreenNav(
    onSaveSettings: (String, String, String) -> Unit
) {
    val context = LocalContext.current
    val sharedPreferences = context.getSharedPreferences("pwnagotchi_prefs", Context.MODE_PRIVATE)
    val host = sharedPreferences.getString("host", "10.0.0.2") ?: "10.0.0.2"
    val apiKey = sharedPreferences.getString("api_key", "") ?: ""
    SettingsScreen(host, apiKey, onSaveSettings)
}
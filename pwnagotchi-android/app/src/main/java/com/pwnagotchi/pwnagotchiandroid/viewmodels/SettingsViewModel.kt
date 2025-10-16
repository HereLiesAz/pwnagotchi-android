package com.pwnagotchi.pwnagotchiandroid.viewmodels

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val sharedPreferences = application.getSharedPreferences("pwnagotchi_prefs", Context.MODE_PRIVATE)

    private val _host = MutableStateFlow(sharedPreferences.getString("host", "10.0.0.2") ?: "10.0.0.2")
    val host: StateFlow<String> = _host

    private val _apiKey = MutableStateFlow(sharedPreferences.getString("opwngrid_api_key", "") ?: "")
    val apiKey: StateFlow<String> = _apiKey

    private val _theme = MutableStateFlow(sharedPreferences.getString("theme", "System") ?: "System")
    val theme: StateFlow<String> = _theme

    fun onHostChanged(newHost: String) {
        _host.value = newHost
    }

    fun onApiKeyChanged(newApiKey: String) {
        _apiKey.value = newApiKey
    }

    fun onThemeChanged(newTheme: String) {
        _theme.value = newTheme
    }

    fun saveSettings() {
        sharedPreferences.edit()
            .putString("host", _host.value)
            .putString("opwngrid_api_key", _apiKey.value)
            .putString("theme", _theme.value)
            .apply()
    }
}
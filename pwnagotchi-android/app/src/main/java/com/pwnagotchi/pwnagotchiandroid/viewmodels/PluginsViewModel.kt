package com.pwnagotchi.pwnagotchiandroid.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.pwnagotchi.pwnagotchiandroid.CommunityPlugin
import com.pwnagotchi.pwnagotchiandroid.Plugin
import com.pwnagotchi.pwnagotchiandroid.PwnagotchiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class PluginsViewModel(application: Application) : AndroidViewModel(application) {
    private val _plugins = MutableStateFlow<List<Plugin>>(emptyList())
    val plugins: StateFlow<List<Plugin>> = _plugins

    private val _communityPlugins = MutableStateFlow<List<CommunityPlugin>>(emptyList())
    val communityPlugins: StateFlow<List<CommunityPlugin>> = _communityPlugins

    private var pwnagotchiService: PwnagotchiService? = null

    fun setService(service: PwnagotchiService?) {
        pwnagotchiService = service
        viewModelScope.launch {
            service?.uiState?.collect { state ->
                if (state is PwnagotchiUiState.Connected) {
                    _plugins.value = state.plugins
                    _communityPlugins.value = state.communityPlugins
                }
            }
        }
    }

    fun togglePlugin(pluginName: String, enabled: Boolean) {
        pwnagotchiService?.sendCommand("{\"command\": \"toggle_plugin\", \"plugin_name\": \"$pluginName\", \"enabled\": $enabled}")
    }

    fun installCommunityPlugin(pluginName: String) {
        pwnagotchiService?.sendCommand("{\"command\": \"install_community_plugin\", \"plugin_name\": \"$pluginName\"}")
    }
}
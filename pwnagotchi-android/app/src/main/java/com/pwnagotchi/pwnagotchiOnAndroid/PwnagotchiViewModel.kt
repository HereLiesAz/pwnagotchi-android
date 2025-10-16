package com.pwnagotchi.pwnagotchiOnAndroid

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PwnagotchiViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<PwnagotchiUiState>(PwnagotchiUiState.Disconnected("Not connected"))
    val uiState: StateFlow<PwnagotchiUiState> = _uiState
    private var pwnagotchiService: PwnagotchiService? = null

    private val _appMode = MutableStateFlow(AppMode.REMOTE)
    val appMode: StateFlow<AppMode> = _appMode

    fun setService(service: PwnagotchiService?) {
        pwnagotchiService = service
        viewModelScope.launch {
            service?.uiState?.collect {
                _uiState.value = it
            }
        }
    }

    fun onModeChange(newMode: AppMode) {
        _appMode.value = newMode
        pwnagotchiService?.setMode(newMode)
    }

    fun startLocalAgent() {
        pwnagotchiService?.startLocalAgent()
    }

    fun stopLocalAgent() {
        pwnagotchiService?.disconnect()
    }
}

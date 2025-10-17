package com.hereliesaz.pwnagotchiOnAndroid

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PwnagotchiViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<PwnagotchiUiState>(PwnagotchiUiState.Disconnected("Not connected"))
    val uiState: StateFlow<PwnagotchiUiState> = _uiState
    private var pwnagotchiService: PwnagotchiService? = null

    fun setService(service: PwnagotchiService?) {
        pwnagotchiService = service
        viewModelScope.launch {
            service?.uiState?.collect {
                _uiState.value = it
            }
        }
    }
}

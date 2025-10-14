package com.pwnagotchi.pwnagotchiandroid

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {
    private val _uiState = MutableStateFlow("Waiting for bettercap...")
    val uiState: StateFlow<String> = _uiState

    fun setService(service: BettercapService) {
        viewModelScope.launch {
            service.uiState.collect {
                _uiState.value = it
            }
        }
    }
}

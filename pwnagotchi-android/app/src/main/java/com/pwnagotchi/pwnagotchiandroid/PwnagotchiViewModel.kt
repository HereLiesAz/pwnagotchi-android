package com.pwnagotchi.pwnagotchiandroid

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class PwnagotchiViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<PwnagotchiUiState>(PwnagotchiUiState.Disconnected("Not connected"))
    val uiState: StateFlow<PwnagotchiUiState> = _uiState
    private var pwnagotchiService: PwnagotchiService? = null

    val opwngridViewModel: OpwngridViewModel by lazy {
        OpwngridViewModel(OpwngridClient())
    }

    fun setService(service: PwnagotchiService?) {
        pwnagotchiService = service
        viewModelScope.launch {
            service?.uiState?.collect {
                _uiState.value = it
            }
        }
    }

    fun fetchLeaderboard() {
        pwnagotchiService?.fetchLeaderboard()
    }
}

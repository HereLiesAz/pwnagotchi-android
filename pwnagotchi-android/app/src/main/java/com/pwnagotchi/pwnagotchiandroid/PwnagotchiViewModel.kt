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
    private lateinit var widgetStateRepository: com.pwnagotchi.pwnagotchiandroid.widgets.WidgetStateRepository

    fun setService(service: PwnagotchiService?) {
        pwnagotchiService = service
        if (service != null) {
            widgetStateRepository = com.pwnagotchi.pwnagotchiandroid.widgets.WidgetStateRepository(service)
        }
        viewModelScope.launch {
            service?.uiState?.collect {
                _uiState.value = it
            }
        }
    }

    fun fetchLeaderboard() {
        pwnagotchiService?.fetchLeaderboard()
    }

    fun updateLeaderboardWidget(leaderboard: String) {
        viewModelScope.launch {
            widgetStateRepository.updateLeaderboard(leaderboard)
        }
    }
}

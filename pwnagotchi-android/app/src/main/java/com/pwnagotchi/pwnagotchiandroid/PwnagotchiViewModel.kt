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

    fun setService(service: PwnagotchiService?) {
        viewModelScope.launch {
            service?.uiState?.collect {
                _uiState.value = it
            }
        }
    }
}

data class Handshake(
    val ap: String,
    val sta: String,
    val filename: String
)

sealed class PwnagotchiUiState {
    data class Connecting(val message: String) : PwnagotchiUiState()
    data class Connected(
        val data: String,
        val handshakes: List<Handshake> = emptyList(),
        val plugins: List<Plugin> = emptyList(),
        val face: String = "(·•᷄_•᷅ ·)",
        val leaderboard: List<LeaderboardEntry> = emptyList(),
        val communityPlugins: List<CommunityPlugin> = emptyList()
    ) : PwnagotchiUiState()
    data class Disconnected(val reason: String) : PwnagotchiUiState()
    data class Error(val message: String) : PwnagotchiUiState()
}

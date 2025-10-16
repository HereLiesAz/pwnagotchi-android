package com.pwnagotchi.pwnagotchiandroid.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.pwnagotchi.pwnagotchiandroid.PwnagotchiService
import com.pwnagotchi.pwnagotchiandroid.PwnagotchiUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class PwnagotchiViewModel(application: Application) : AndroidViewModel(application) {
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

    fun disconnect() {
        pwnagotchiService?.disconnect()
    }
}
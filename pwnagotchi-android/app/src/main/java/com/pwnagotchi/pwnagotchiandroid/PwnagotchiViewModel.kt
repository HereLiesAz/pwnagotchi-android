package com.pwnagotchi.pwnagotchiandroid

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

/**
 * The ViewModel for the main Pwnagotchi screen. It acts as the primary bridge between the
 * UI layer (Composables) and the data/business logic layer (`PwnagotchiService`).
 *
 * This ViewModel exposes the `PwnagotchiUiState` to the UI and provides functions for the
 * UI to send commands and interact with the Pwnagotchi service.
 *
 * FUTURE ARCHITECTURE:
 * This ViewModel will be expanded to manage the state for the dual-mode architecture.
 * Key additions will include:
 * -   `appMode`: A `StateFlow` to track whether the app is in `LOCAL` or `REMOTE` mode.
 * -   `onModeChange(AppMode)`: A function to allow the UI to switch between modes.
 * -   Functions to control the local agent (e.g., `startLocalAgent`, `stopLocalAgent`) which
 *     will delegate calls to the `PwnagotchiService`.
 */
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

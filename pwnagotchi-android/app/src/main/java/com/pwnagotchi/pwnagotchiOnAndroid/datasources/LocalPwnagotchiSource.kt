package com.pwnagotchi.pwnagotchiOnAndroid.datasources

import android.content.Context
import com.pwnagotchi.pwnagotchiOnAndroid.PwnagotchiUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * A placeholder data source for the "Local Agent" mode.
 *
 * This class will eventually contain the logic for running `bettercap` natively on a rooted
 * Android device. For now, it serves as a non-functional placeholder to allow the
 * dual-mode architecture to be built.
 */
class LocalPwnagotchiSource(private val context: Context) : PwnagotchiDataSource {
    private val _uiState = MutableStateFlow<PwnagotchiUiState>(PwnagotchiUiState.Disconnected("Local Agent not started"))
    override val uiState: StateFlow<PwnagotchiUiState> = _uiState

    override suspend fun start(params: DataSourceParams?) {
        // TODO: Implement in a future phase
    }

    override suspend fun stop() {
        // TODO: Implement in a future phase
    }

    override suspend fun sendCommand(command: String) {
        // TODO: Implement in a future phase
    }
}

package com.hereliesaz.pwnagotchiOnAndroid.datasources

import android.content.Context
import com.hereliesaz.pwnagotchiOnAndroid.PwnagotchiUiState
import com.hereliesaz.pwnagotchiOnAndroid.R
import com.hereliesaz.pwnagotchiOnAndroid.core.LocalAgentManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * A data source for the "Local Agent" mode.
 *
 * This class uses the [LocalAgentManager] to execute root commands to enable monitor mode
 * and eventually run the `bettercap` process.
 */
class LocalPwnagotchiSource(private val context: Context) : PwnagotchiDataSource {
    private val _uiState = MutableStateFlow<PwnagotchiUiState>(PwnagotchiUiState.Disconnected(context.getString(R.string.status_local_agent_not_started)))
    override val uiState: StateFlow<PwnagotchiUiState> = _uiState

    override suspend fun start(params: DataSourceParams?) {
        _uiState.value = PwnagotchiUiState.Connecting(context.getString(R.string.status_enabling_monitor_mode))
        if (LocalAgentManager.enableMonitorMode()) {
            _uiState.value = PwnagotchiUiState.Connected(context.getString(R.string.status_monitor_mode_enabled), emptyList(), emptyList(), "", emptyList(), emptyList())
            // TODO: Launch bettercap process
        } else {
            _uiState.value = PwnagotchiUiState.Error(context.getString(R.string.error_failed_to_enable_monitor_mode))
        }
    }

    override suspend fun stop() {
        _uiState.value = PwnagotchiUiState.Connecting(context.getString(R.string.status_disabling_monitor_mode))
        if (LocalAgentManager.disableMonitorMode()) {
            _uiState.value = PwnagotchiUiState.Disconnected(context.getString(R.string.status_local_agent_stopped))
        } else {
            _uiState.value = PwnagotchiUiState.Error(context.getString(R.string.error_failed_to_disable_monitor_mode))
        }
    }

    override suspend fun sendCommand(command: String) {
        // Not applicable for local agent mode
    }
}

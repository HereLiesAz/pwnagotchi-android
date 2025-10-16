package com.pwnagotchi.pwnagotchiOnAndroid.datasources

import com.pwnagotchi.pwnagotchiOnAndroid.PwnagotchiUiState
import kotlinx.coroutines.flow.StateFlow

/**
 * An interface that defines the contract for all Pwnagotchi data sources.
 *
 * This abstraction allows the application to seamlessly switch between different
 * modes of operation (e.g., a remote WebSocket client or a local, native agent)
 * by providing a unified API for starting, stopping, and interacting with the
 * Pwnagotchi backend.
 */
interface PwnagotchiDataSource {
    /**
     * The current UI state of the data source, exposed as a StateFlow.
     */
    val uiState: StateFlow<PwnagotchiUiState>

    /**
     * Starts the data source.
     *
     * @param params The parameters required for starting the source, encapsulated in a
     *               type-safe sealed class. This is nullable for data sources that do not
     *               require parameters (e.g., the local agent).
     */
    suspend fun start(params: DataSourceParams? = null)

    /**
     * Stops the data source and cleans up any resources.
     */
    suspend fun stop()

    /**
     * Sends a command to the Pwnagotchi backend.
     *
     * @param command The command to be sent as a string.
     */
    suspend fun sendCommand(command: String)
}

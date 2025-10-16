package com.pwnagotchi.pwnagotchiandroid.datasources

import com.pwnagotchi.pwnagotchiandroid.PwnagotchiUiState
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
     * @param params A map of optional parameters required for starting the source
     *               (e.g., a URI for a remote connection).
     */
    suspend fun start(params: Map<String, Any> = emptyMap())

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
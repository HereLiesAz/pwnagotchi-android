package com.hereliesaz.pwnagotchiOnAndroid

import kotlinx.serialization.Serializable

@Serializable
data class Handshake(val ap: String, val sta: String, val filename: String)

@Serializable
data class Plugin(val name: String, val enabled: Boolean)

@Serializable
data class CommunityPlugin(val name: String, val description: String)

@Serializable
data class LeaderboardEntry(val name: String, val handshakes: Int, val rank: Int)

sealed class PwnagotchiUiState {
    data class Disconnected(val message: String) : PwnagotchiUiState()
    data class Connecting(val message: String) : PwnagotchiUiState()
    data class Connected(
        val status: String,
        val handshakes: List<Handshake>,
        val plugins: List<Plugin>,
        val face: String,
        val leaderboard: List<LeaderboardEntry>,
        val communityPlugins: List<CommunityPlugin>
    ) : PwnagotchiUiState()
    data class Error(val message: String) : PwnagotchiUiState()
}

@Serializable
data class BaseMessage(val type: String)

@Serializable
data class UiUpdateMessage(val type: String, val data: UiUpdateData)

@Serializable
data class UiUpdateData(val face: String, val channel: String, val aps: String, val uptime: String, val shakes: String, val mode: String)

@Serializable
data class HandshakeMessage(val type: String, val data: HandshakeData)

@Serializable
data class HandshakeData(val ap: ApData, val sta: StaData, val filename: String)

@Serializable
data class ApData(val hostname: String)

@Serializable
data class StaData(val mac: String)

@Serializable
data class PluginData(val name: String, val enabled: Boolean)

@Serializable
data class PluginListMessage(val type: String, val data: List<PluginData>)

@Serializable
data class CommunityPluginData(val name: String, val description: String)

@Serializable
data class CommunityPluginListMessage(val type: String, val data: List<CommunityPluginData>)

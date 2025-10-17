package com.hereliesaz.pwnagotchiOnAndroid

data class Plugin(val name: String, val enabled: Boolean)
data class CommunityPlugin(val name: String, val description: String)
data class Handshake(val ap: String, val sta: String, val filename: String)
data class LeaderboardEntry(val rank: Int, val name: String?, val handshakes: Int)

sealed interface PwnagotchiUiState {
    data class Connected(
        val status: String,
        val handshakes: List<Handshake>,
        val plugins: List<Plugin>,
        val face: String,
        val leaderboard: List<LeaderboardEntry>,
        val communityPlugins: List<CommunityPlugin>
    ) : PwnagotchiUiState
    data class Disconnected(val status: String) : PwnagotchiUiState {}
    data class Connecting(val status: String) : PwnagotchiUiState {}
    data class Error(val message: String) : PwnagotchiUiState {}
}

package com.pwnagotchi.pwnagotchiandroid

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BaseMessage(val type: String)

@Serializable
data class UiUpdateData(
    val face: String,
    val channel: String,
    val aps: String,
    val uptime: String,
    val shakes: String,
    val mode: String
)

@Serializable
data class UiUpdateMessage(
    val type: String,
    val data: UiUpdateData
)

@Serializable
data class AccessPoint(
    val hostname: String
)

@Serializable
data class Station(
    val mac: String
)

@Serializable
data class HandshakeData(
    val ap: AccessPoint,
    val sta: Station,
    val filename: String
)

@Serializable
data class HandshakeMessage(
    val type: String,
    val data: HandshakeData
)

@Serializable
data class PluginData(
    val name: String,
    val enabled: Boolean
)

@Serializable
data class PluginListMessage(
    val type: String,
    val data: List<PluginData>
)

@Serializable
data class CommunityPluginData(
    val name: String,
    val description: String
)

@Serializable
data class CommunityPluginListMessage(
    val type: String,
    val data: List<CommunityPluginData>
)

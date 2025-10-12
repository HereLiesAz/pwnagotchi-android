package com.pwnagotchi.pwnagotchiandroid

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable

@Serializable
data class LeaderboardEntry(
    val name: String,
    val pwns: Int
)

class OpwngridClient {
    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json()
        }
    }

    suspend fun getLeaderboard(): List<LeaderboardEntry> {
        // TODO: Find the correct API endpoint for the oPwngrid leaderboard.
        // This is a placeholder URL.
        val url = "https://opwngrid.xyz/api/leaderboard"
        return try {
            client.get(url).body()
        } catch (e: Exception) {
            // In a real app, handle exceptions gracefully
            e.printStackTrace()
            emptyList()
        }
    }
}

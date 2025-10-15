package com.pwnagotchi.pwnagotchiandroid

import com.pwnagotchi.pwnagotchiandroid.core.Constants
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

class OpwngridClient {
    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
            })
        }
    }

    suspend fun getLeaderboard(): List<LeaderboardEntry> {
        return try {
            // Placeholder URL, as the real API is unknown
            client.get(Constants.OPWNGRID_API_BASE_URL + "leaderboard").body()
        } catch (e: Exception) {
            // In a real app, this would be handled more gracefully
            emptyList()
        }
    }
}

package com.pwnagotchi.pwnagotchiOnAndroid

import android.content.Context
import com.pwnagotchi.pwnagotchiOnAndroid.core.Constants
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

class OpwngridClient(private val context: Context) {
    private val client = HttpClient(Android) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
            })
        }
    }

    suspend fun getLeaderboard(): List<LeaderboardEntry> {
        val sharedPreferences = context.getSharedPreferences("pwnagotchi_prefs", Context.MODE_PRIVATE)
        val apiKey = sharedPreferences.getString("opwngrid_api_key", "") ?: ""
        return try {
            client.get(Constants.OPWNGRID_API_BASE_URL + "leaderboard") {
                header("X-API-KEY", apiKey)
            }.body()
        } catch (e: Exception) {
            emptyList()
        }
    }
}

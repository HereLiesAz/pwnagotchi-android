package com.hereliesaz.pwnagotchiOnAndroid

import android.content.Context
import com.hereliesaz.pwnagotchiOnAndroid.core.Constants
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.jsoup.Jsoup

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
            val html: String = client.get(Constants.OPWNGRID_LEADERBOARD_URL).body()
            val doc = Jsoup.parse(html)
            val table = doc.select("table").first()
            val rows = table?.select("tr")

            val leaderboard = mutableListOf<LeaderboardEntry>()
            for (i in 1 until (rows?.size ?: )) { // Skip header row
                val row = rows?.get(i)
                val cols = row?.select("td")
              cols?.size?.let {
                if (it >= 3) {
                  val rank = cols?.get(0)?.text()?.toIntOrNull() ?: 0
                  val name = cols?.get(1)?.text()
                  val handshakes = cols[2].text().toIntOrNull() ?: 0
                  leaderboard.add(LeaderboardEntry(rank, name, handshakes))
                }
              }
            }
            leaderboard
        } catch (e: Exception) {
            emptyList()
        }
    }
}

package com.pwnagotchi.pwnagotchiOnAndroid

import android.content.Context
import com.pwnagotchi.pwnagotchiOnAndroid.core.Constants
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.android.Android
import io.ktor.client.request.get
import org.jsoup.Jsoup

class OpwngridClient(private val context: Context) {
    private val client = HttpClient(Android)

    suspend fun getLeaderboard(): List<LeaderboardEntry> {
        return try {
            val html: String = client.get(Constants.OPWNGRID_LEADERBOARD_URL).body()
            val doc = Jsoup.parse(html)
            val table = doc.select("table").first()
            val rows = table.select("tr")

            val leaderboard = mutableListOf<LeaderboardEntry>()
            for (i in 1 until rows.size) { // Skip header row
                val row = rows[i]
                val cols = row.select("td")
                if (cols.size >= 3) {
                    val rank = cols[0].text().toIntOrNull() ?: 0
                    val name = cols[1].text()
                    val handshakes = cols[2].text().toIntOrNull() ?: 0
                    leaderboard.add(LeaderboardEntry(rank, name, handshakes))
                }
            }
            leaderboard
        } catch (e: Exception) {
            // Log the exception or handle it as needed
            emptyList()
        }
    }
}
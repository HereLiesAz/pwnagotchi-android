package com.pwnagotchi.pwnagotchiandroid.widgets

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "widget_state")

class WidgetStateRepository(private val context: Context) {
    private object PreferencesKeys {
        val FACE = stringPreferencesKey("face")
        val MESSAGE = stringPreferencesKey("message")
        val HANDSHAKES = stringPreferencesKey("handshakes")
        val LEADERBOARD = stringPreferencesKey("leaderboard")
    }

    val widgetState = context.dataStore.data.map { preferences ->
        WidgetState(
            face = preferences[PreferencesKeys.FACE] ?: "(·•᷄_•᷅ ·)",
            message = preferences[PreferencesKeys.MESSAGE] ?: "Not connected",
            handshakes = preferences[PreferencesKeys.HANDSHAKES] ?: "[]",
            leaderboard = preferences[PreferencesKeys.LEADERBOARD] ?: "[]"
        )
    }

    suspend fun updateFace(face: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.FACE] = face
        }
    }

    suspend fun updateMessage(message: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.MESSAGE] = message
        }
    }

    suspend fun updateHandshakes(handshakes: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.HANDSHAKES] = handshakes
        }
    }

    suspend fun updateLeaderboard(leaderboard: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.LEADERBOARD] = leaderboard
        }
    }
}

data class WidgetState(
    val face: String,
    val message: String,
    val handshakes: String,
    val leaderboard: String
)

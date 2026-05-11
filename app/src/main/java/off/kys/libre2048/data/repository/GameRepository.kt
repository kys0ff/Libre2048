package off.kys.libre2048.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import off.kys.libre2048.domain.model.GameState

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "game_prefs")

class GameRepository(private val context: Context) {

    private val json = Json { ignoreUnknownKeys = true }

    companion object {
        private const val HIGH_SCORE_KEY_PREFIX = "high_score_"
        private const val STATE_KEY_PREFIX = "current_state_"
    }

    fun getHighScore(rows: Int, cols: Int): Flow<Int> {
        val key = intPreferencesKey("${HIGH_SCORE_KEY_PREFIX}${rows}x${cols}")
        return context.dataStore.data.map { preferences ->
            preferences[key] ?: 0
        }
    }

    suspend fun saveHighScore(rows: Int, cols: Int, score: Int) {
        val key = intPreferencesKey("${HIGH_SCORE_KEY_PREFIX}${rows}x${cols}")
        context.dataStore.edit { preferences ->
            val currentHigh = preferences[key] ?: 0
            if (score > currentHigh) {
                preferences[key] = score
            }
        }
    }

    fun getCurrentState(rows: Int, cols: Int): Flow<GameState?> = context.dataStore.data.map { preferences ->
        val key = stringPreferencesKey("${STATE_KEY_PREFIX}${rows}x${cols}")
        preferences[key]?.let {
            try {
                json.decodeFromString<GameState>(it)
            } catch (_: Exception) {
                null
            }
        }
    }

    suspend fun saveCurrentState(rows: Int, cols: Int, state: GameState?) {
        val key = stringPreferencesKey("${STATE_KEY_PREFIX}${rows}x${cols}")
        context.dataStore.edit { preferences ->
            if (state == null) {
                preferences.remove(key)
            } else {
                preferences[key] = json.encodeToString(state)
            }
        }
    }
}
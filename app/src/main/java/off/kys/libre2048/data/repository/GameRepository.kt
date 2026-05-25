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
import off.kys.libre2048.domain.model.GameMode
import off.kys.libre2048.domain.model.GameScore
import off.kys.libre2048.domain.model.GameState

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "game_prefs")

class GameRepository(private val context: Context) {

    private val json = Json { ignoreUnknownKeys = true }

    companion object {
        private const val HIGH_SCORE_KEY_PREFIX = "high_score_"
        private const val STATE_KEY_PREFIX = "current_state_"
        private const val HISTORY_KEY_PREFIX = "history_"
        private val ALL_SCORES_KEY = stringPreferencesKey("all_scores")
    }

    fun getHighScore(rows: Int, cols: Int, mode: GameMode): Flow<Int> {
        val key = intPreferencesKey("${HIGH_SCORE_KEY_PREFIX}${rows}x${cols}_${mode.name}")
        return context.dataStore.data.map { preferences ->
            preferences[key] ?: 0
        }
    }

    suspend fun saveHighScore(rows: Int, cols: Int, mode: GameMode, score: Int) {
        val key = intPreferencesKey("${HIGH_SCORE_KEY_PREFIX}${rows}x${cols}_${mode.name}")
        context.dataStore.edit { preferences ->
            val currentHigh = preferences[key] ?: 0
            if (score > currentHigh) {
                preferences[key] = score
            }
        }
    }

    fun getAllScores(): Flow<List<GameScore>> = context.dataStore.data.map { preferences ->
        preferences[ALL_SCORES_KEY]?.let {
            try {
                json.decodeFromString<List<GameScore>>(it)
            } catch (_: Exception) {
                emptyList()
            }
        } ?: emptyList()
    }

    suspend fun saveScore(gameScore: GameScore) {
        context.dataStore.edit { preferences ->
            val currentScores = preferences[ALL_SCORES_KEY]?.let {
                try {
                    json.decodeFromString<List<GameScore>>(it)
                } catch (_: Exception) {
                    emptyList()
                }
            } ?: emptyList()
            val newScores = currentScores + gameScore
            preferences[ALL_SCORES_KEY] = json.encodeToString(newScores)
        }
    }

    suspend fun deleteDuplicateScores() {
        context.dataStore.edit { preferences ->
            val currentScores = preferences[ALL_SCORES_KEY]?.let {
                try {
                    json.decodeFromString<List<GameScore>>(it)
                } catch (_: Exception) {
                    emptyList()
                }
            } ?: emptyList()

            if (currentScores.isEmpty()) return@edit

            // Deduplicate: Keep only the most recent entry for each unique combination of score and game configuration
            val uniqueScores = currentScores
                .groupBy { "${it.score}_${it.rows}_${it.cols}_${it.mode}" }
                .map { (_, scores) -> scores.maxByOrNull { it.date }!! }
                .sortedByDescending { it.date }

            if (uniqueScores.size < currentScores.size) {
                preferences[ALL_SCORES_KEY] = json.encodeToString(uniqueScores)
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

    fun getHistory(rows: Int, cols: Int): Flow<List<GameState>> =
        context.dataStore.data.map { preferences ->
            val key = stringPreferencesKey("${HISTORY_KEY_PREFIX}${rows}x${cols}")
            preferences[key]?.let {
                try {
                    json.decodeFromString<List<GameState>>(it)
                } catch (_: Exception) {
                    emptyList()
                }
            } ?: emptyList()
        }

    suspend fun saveHistory(rows: Int, cols: Int, history: List<GameState>) {
        val key = stringPreferencesKey("${HISTORY_KEY_PREFIX}${rows}x${cols}")
        context.dataStore.edit { preferences ->
            if (history.isEmpty()) {
                preferences.remove(key)
            } else {
                preferences[key] = json.encodeToString(history)
            }
        }
    }
}
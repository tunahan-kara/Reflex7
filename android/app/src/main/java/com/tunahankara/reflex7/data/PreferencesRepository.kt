package com.tunahankara.reflex7.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.tunahankara.reflex7.engine.GameMode
import com.tunahankara.reflex7.engine.ModeRecord
import com.tunahankara.reflex7.engine.PlayerPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.dataStore by preferencesDataStore("reflex7_preferences")

class PreferencesRepository(private val context: Context) {
    private object Keys {
        val nickname = stringPreferencesKey("nickname")
        val sound = booleanPreferencesKey("sound")
        val language = stringPreferencesKey("language")
        val onboarding = booleanPreferencesKey("onboarding_seen")
        val discoveries = stringPreferencesKey("discoveries")
        val slowLevel = intPreferencesKey("best_level_7")
        val slowScore = longPreferencesKey("best_score_7")
        val fastLevel = intPreferencesKey("best_level_4")
        val fastScore = longPreferencesKey("best_score_4")
    }

    val preferences: Flow<PlayerPreferences> = context.dataStore.data
        .catch { error -> if (error is IOException) emit(androidx.datastore.preferences.core.emptyPreferences()) else throw error }
        .map { values ->
            PlayerPreferences(
                nickname = values[Keys.nickname].orEmpty(),
                soundEnabled = values[Keys.sound] ?: true,
                language = values[Keys.language] ?: "tr",
                onboardingSeen = values[Keys.onboarding] ?: false,
                discoveries = values[Keys.discoveries].orEmpty().split('|').filter(String::isNotBlank).toSet(),
                slowRecord = ModeRecord(values[Keys.slowLevel] ?: 0, values[Keys.slowScore] ?: 0),
                fastRecord = ModeRecord(values[Keys.fastLevel] ?: 0, values[Keys.fastScore] ?: 0)
            )
        }

    suspend fun setNickname(value: String) = context.dataStore.edit { it[Keys.nickname] = value.take(12) }
    suspend fun setSound(value: Boolean) = context.dataStore.edit { it[Keys.sound] = value }
    suspend fun setLanguage(value: String) = context.dataStore.edit { it[Keys.language] = value }
    suspend fun setOnboardingSeen() = context.dataStore.edit { it[Keys.onboarding] = true }
    suspend fun setDiscoveries(values: Set<String>) = context.dataStore.edit { it[Keys.discoveries] = values.joinToString("|") }

    suspend fun saveRecord(mode: GameMode, record: ModeRecord) = context.dataStore.edit { values ->
        if (mode == GameMode.SLOW) {
            values[Keys.slowLevel] = record.bestLevel
            values[Keys.slowScore] = record.bestScore
        } else {
            values[Keys.fastLevel] = record.bestLevel
            values[Keys.fastScore] = record.bestScore
        }
    }
}

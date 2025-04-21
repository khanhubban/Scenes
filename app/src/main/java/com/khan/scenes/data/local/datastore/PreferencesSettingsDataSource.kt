package com.khan.scenes.data.local.datastore // Adjust package if needed

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import com.khan.scenes.data.local.SettingsLocalDataSource
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton // Make this a singleton as it manages access to the single DataStore
class PreferencesSettingsDataSource @Inject constructor(
    @ApplicationContext private val context: Context // Inject context to access dataStore
) : SettingsLocalDataSource {

    // Expose settings as Flows, reading from dataStore using PreferencesKeys
    override val darkThemeEnabledFlow: Flow<Boolean> = context.dataStore.data
        .catch { exception -> // Handle potential IOExceptions reading the file
            if (exception is IOException) {
                emit(emptyPreferences()) // Emit empty preferences if file not found or corrupt
            } else {
                throw exception // Rethrow other exceptions
            }
        }
        .map { preferences ->
            // Read the boolean value associated with DARK_THEME_ENABLED key
            // Provide a default value (false) if the key doesn't exist
            preferences[PreferencesKeys.DARK_THEME_ENABLED] ?: false
        }

    override val autoChangeIntervalFlow: Flow<String> = context.dataStore.data
        .catch { exception -> if (exception is IOException) emit(emptyPreferences()) else throw exception }
        .map { preferences ->
            // Read String value, provide default "never"
            preferences[PreferencesKeys.AUTO_CHANGE_INTERVAL] ?: "never"
        }

    override val autoChangeSourceFlow: Flow<String> = context.dataStore.data
        .catch { exception -> if (exception is IOException) emit(emptyPreferences()) else throw exception }
        .map { preferences ->
            // Read String value, provide default "favorites"
            preferences[PreferencesKeys.AUTO_CHANGE_SOURCE] ?: "favorites"
        }


    // Implement suspend functions to update settings using dataStore.edit
    override suspend fun setDarkThemeEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DARK_THEME_ENABLED] = enabled
        }
    }

    override suspend fun setAutoChangeInterval(interval: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.AUTO_CHANGE_INTERVAL] = interval
        }
    }

    override suspend fun setAutoChangeSource(source: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.AUTO_CHANGE_SOURCE] = source
        }
    }
}
package com.khan.scenes.data.local.datastore

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

@Singleton
class PreferencesSettingsDataSource @Inject constructor(
    @ApplicationContext private val context: Context
) : SettingsLocalDataSource {

    override val darkThemeEnabledFlow: Flow<Boolean> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[PreferencesKeys.DARK_THEME_ENABLED] ?: false
        }

    override val autoChangeIntervalFlow: Flow<String> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences ->
            preferences[PreferencesKeys.AUTO_CHANGE_INTERVAL] ?: "never"
        }

    override val autoChangeSourceFlow: Flow<String> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences ->
            preferences[PreferencesKeys.AUTO_CHANGE_SOURCE] ?: "favorites"
        }

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
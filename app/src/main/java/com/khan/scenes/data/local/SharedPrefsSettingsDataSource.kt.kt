package com.khan.scenes.data.local

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of SettingsLocalDataSource using SharedPreferences
 */
@Singleton
class SharedPrefsSettingsDataSource @Inject constructor(
    @ApplicationContext private val context: Context
) : SettingsLocalDataSource {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("scenes_settings", Context.MODE_PRIVATE)

    // Keys for SharedPreferences
    companion object {
        private const val KEY_DARK_THEME = "dark_theme"
        private const val KEY_AUTO_CHANGE_INTERVAL = "auto_change_interval"
        private const val KEY_AUTO_CHANGE_SOURCE = "auto_change_source"

        // Default values
        private const val DEFAULT_AUTO_CHANGE_INTERVAL = "Never"
        private const val DEFAULT_AUTO_CHANGE_SOURCE = "None"
    }

    // StateFlows to expose settings as observable streams
    private val _darkThemeEnabled = MutableStateFlow(
        sharedPreferences.getBoolean(KEY_DARK_THEME, false)
    )

    private val _autoChangeInterval = MutableStateFlow(
        sharedPreferences.getString(KEY_AUTO_CHANGE_INTERVAL, DEFAULT_AUTO_CHANGE_INTERVAL)
            ?: DEFAULT_AUTO_CHANGE_INTERVAL
    )

    private val _autoChangeSource = MutableStateFlow(
        sharedPreferences.getString(KEY_AUTO_CHANGE_SOURCE, DEFAULT_AUTO_CHANGE_SOURCE)
            ?: DEFAULT_AUTO_CHANGE_SOURCE
    )

    // Interface implementations - renamed from original properties to match interface
    override val darkThemeEnabledFlow: Flow<Boolean> = _darkThemeEnabled
    override val autoChangeIntervalFlow: Flow<String> = _autoChangeInterval
    override val autoChangeSourceFlow: Flow<String> = _autoChangeSource

    // Functions to update settings - make them suspend functions to match interface
    override suspend fun setDarkThemeEnabled(enabled: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_DARK_THEME, enabled).apply()
        _darkThemeEnabled.value = enabled
    }

    override suspend fun setAutoChangeInterval(interval: String) {
        sharedPreferences.edit().putString(KEY_AUTO_CHANGE_INTERVAL, interval).apply()
        _autoChangeInterval.value = interval
    }

    override suspend fun setAutoChangeSource(source: String) {
        sharedPreferences.edit().putString(KEY_AUTO_CHANGE_SOURCE, source).apply()
        _autoChangeSource.value = source
    }
}
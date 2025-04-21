package com.khan.scenes.data.local

import kotlinx.coroutines.flow.Flow

/**
 * Interface for accessing local app settings
 */
interface SettingsLocalDataSource {
    // Settings as observable flows
    val darkThemeEnabledFlow: Flow<Boolean>
    val autoChangeIntervalFlow: Flow<String>
    val autoChangeSourceFlow: Flow<String>

    // Methods to update settings
    suspend fun setDarkThemeEnabled(enabled: Boolean)
    suspend fun setAutoChangeInterval(interval: String)
    suspend fun setAutoChangeSource(source: String)
}
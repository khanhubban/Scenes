package com.khan.scenes.data.local // Adjust package if needed

import kotlinx.coroutines.flow.Flow

interface SettingsLocalDataSource {
    // Flows to observe setting changes
    val darkThemeEnabledFlow: Flow<Boolean>
    val autoChangeIntervalFlow: Flow<String>
    val autoChangeSourceFlow: Flow<String>

    // Suspend functions to update settings
    suspend fun setDarkThemeEnabled(enabled: Boolean)
    suspend fun setAutoChangeInterval(interval: String)
    suspend fun setAutoChangeSource(source: String)
}
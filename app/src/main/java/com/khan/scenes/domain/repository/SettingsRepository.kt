package com.khan.scenes.domain.repository // Adjust package if needed

import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val darkThemeEnabledFlow: Flow<Boolean>
    val autoChangeIntervalFlow: Flow<String>
    val autoChangeSourceFlow: Flow<String>

    suspend fun setDarkThemeEnabled(enabled: Boolean)
    suspend fun setAutoChangeInterval(interval: String)
    suspend fun setAutoChangeSource(source: String)
}
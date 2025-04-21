package com.khan.scenes.domain.repository

import kotlinx.coroutines.flow.StateFlow

interface SettingsRepository {
    // Flows to observe settings
    val darkThemeEnabledFlow: StateFlow<Boolean>
    val autoChangeIntervalFlow: StateFlow<String>
    val autoChangeSourceFlow: StateFlow<String>

    // Functions to update settings
    suspend fun setDarkThemeEnabled(enabled: Boolean)
    suspend fun setAutoChangeInterval(interval: String)
    suspend fun setAutoChangeSource(source: String)
}
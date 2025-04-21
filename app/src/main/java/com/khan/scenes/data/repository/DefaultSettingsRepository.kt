package com.khan.scenes.data.repository // Adjust package if needed

import com.khan.scenes.data.local.SettingsLocalDataSource
import com.khan.scenes.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultSettingsRepository @Inject constructor(
    private val settingsLocalDataSource: SettingsLocalDataSource // Inject DataStore source
) : SettingsRepository {

    override val darkThemeEnabledFlow: Flow<Boolean>
        get() = settingsLocalDataSource.darkThemeEnabledFlow

    override val autoChangeIntervalFlow: Flow<String>
        get() = settingsLocalDataSource.autoChangeIntervalFlow

    override val autoChangeSourceFlow: Flow<String>
        get() = settingsLocalDataSource.autoChangeSourceFlow

    override suspend fun setDarkThemeEnabled(enabled: Boolean) {
        settingsLocalDataSource.setDarkThemeEnabled(enabled)
    }

    override suspend fun setAutoChangeInterval(interval: String) {
        settingsLocalDataSource.setAutoChangeInterval(interval)
    }

    override suspend fun setAutoChangeSource(source: String) {
        settingsLocalDataSource.setAutoChangeSource(source)
    }
}
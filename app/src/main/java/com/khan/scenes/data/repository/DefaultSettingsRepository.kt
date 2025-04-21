package com.khan.scenes.data.repository

import com.khan.scenes.data.local.SettingsLocalDataSource
import com.khan.scenes.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Inject

class DefaultSettingsRepository @Inject constructor(
    private val localDataSource: SettingsLocalDataSource
) : SettingsRepository {
    // Create a coroutine scope for the repository
    private val repositoryScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    // Convert Flow to StateFlow by calling stateIn
    override val darkThemeEnabledFlow: StateFlow<Boolean> =
        localDataSource.darkThemeEnabledFlow.stateIn(
            repositoryScope,
            kotlinx.coroutines.flow.SharingStarted.Eagerly,
            false // Default value
        )

    override val autoChangeIntervalFlow: StateFlow<String> =
        localDataSource.autoChangeIntervalFlow.stateIn(
            repositoryScope,
            kotlinx.coroutines.flow.SharingStarted.Eagerly,
            "never" // Default value
        )

    override val autoChangeSourceFlow: StateFlow<String> =
        localDataSource.autoChangeSourceFlow.stateIn(
            repositoryScope,
            kotlinx.coroutines.flow.SharingStarted.Eagerly,
            "favorites" // Default value
        )

    override suspend fun setDarkThemeEnabled(enabled: Boolean) {
        localDataSource.setDarkThemeEnabled(enabled)
    }

    override suspend fun setAutoChangeInterval(interval: String) {
        localDataSource.setAutoChangeInterval(interval)
    }

    override suspend fun setAutoChangeSource(source: String) {
        localDataSource.setAutoChangeSource(source)
    }
}
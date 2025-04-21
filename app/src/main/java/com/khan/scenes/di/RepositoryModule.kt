package com.khan.scenes.di

import com.khan.scenes.data.local.SettingsLocalDataSource
import com.khan.scenes.data.local.datastore.PreferencesSettingsDataSource
import com.khan.scenes.data.remote.KtorWallpaperRemoteDataSource
import com.khan.scenes.data.remote.WallpaperRemoteDataSource
import com.khan.scenes.data.repository.DefaultFavoritesRepository
import com.khan.scenes.data.repository.DefaultSettingsRepository
import com.khan.scenes.data.repository.DefaultWallpaperRepository
import com.khan.scenes.domain.repository.FavoritesRepository
import com.khan.scenes.domain.repository.SettingsRepository
import com.khan.scenes.domain.repository.WallpaperRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindWallpaperRepository(
        defaultWallpaperRepository: DefaultWallpaperRepository
    ): WallpaperRepository

    @Binds
    @Singleton
    abstract fun bindFavoritesRepository(
        defaultFavoritesRepository: DefaultFavoritesRepository
    ): FavoritesRepository

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(
        defaultSettingsRepository: DefaultSettingsRepository
    ): SettingsRepository

    @Binds
    @Singleton
    abstract fun bindWallpaperRemoteDataSource(
        ktorDataSource: KtorWallpaperRemoteDataSource
    ): WallpaperRemoteDataSource

    // Add this binding for your SettingsLocalDataSource
    @Binds
    @Singleton
    abstract fun bindSettingsLocalDataSource(
        // Choose which implementation you want to use:
        dataSource: PreferencesSettingsDataSource // DataStore implementation
        // OR
        // dataSource: SharedPrefsSettingsDataSource // SharedPreferences implementation
    ): SettingsLocalDataSource
}
package com.khan.scenes.di // Adjust package if needed

import com.khan.scenes.data.remote.KtorWallpaperRemoteDataSource // *** Import implementation ***
import com.khan.scenes.data.remote.WallpaperRemoteDataSource // *** Import interface ***
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
@InstallIn(SingletonComponent::class) // Bindings will live as long as the application
abstract class RepositoryModule {

    // --- Existing Bindings ---

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

    // *** ADD THIS BINDING ***
    @Binds
    @Singleton // Assuming you want a single instance of the remote data source
    abstract fun bindWallpaperRemoteDataSource(
        ktorDataSource: KtorWallpaperRemoteDataSource // Implementation class as parameter
    ): WallpaperRemoteDataSource // Interface as return type
    // ***********************

}
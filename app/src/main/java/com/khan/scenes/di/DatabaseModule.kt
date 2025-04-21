package com.khan.scenes.di // Adjust package if needed

import android.content.Context
import androidx.room.Room
import com.khan.scenes.data.local.db.AppDatabase
import com.khan.scenes.data.local.db.dao.FavoritesDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class) // App-scoped singletons
object DatabaseModule {

    @Provides
    @Singleton // Provide a single instance of the database
    fun provideAppDatabase(@ApplicationContext appContext: Context): AppDatabase { // Hilt provides context
        return Room.databaseBuilder(
            appContext,
            AppDatabase::class.java,
            "wallpaper_database" // Name of the database file
        )
            // TODO: Implement proper migrations for production instead of fallback
            // .fallbackToDestructiveMigration() // Simple strategy for development
            .build()
    }

    @Provides
    // No need for @Singleton here, DAO lives as long as the Singleton AppDatabase
    fun provideFavoritesDao(appDatabase: AppDatabase): FavoritesDao {
        return appDatabase.favoritesDao() // Get DAO from database instance
    }
}
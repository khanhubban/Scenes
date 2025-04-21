package com.khan.scenes.data.local.db // Adjust package if needed

import androidx.room.Database
import androidx.room.RoomDatabase
import com.khan.scenes.data.local.db.dao.FavoritesDao
import com.khan.scenes.data.local.db.entity.FavoriteWallpaperEntity

@Database(
    entities = [FavoriteWallpaperEntity::class], // List all entities for this DB
    version = 1,                                // Schema version
    exportSchema = true                         // Export schema to JSON files
)
abstract class AppDatabase : RoomDatabase() {

    // Abstract function providing access to the DAO
    abstract fun favoritesDao(): FavoritesDao

}
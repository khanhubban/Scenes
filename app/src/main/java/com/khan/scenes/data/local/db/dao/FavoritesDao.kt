package com.khan.scenes.data.local.db.dao // Adjust package if needed

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.khan.scenes.data.local.db.entity.FavoriteWallpaperEntity
import kotlinx.coroutines.flow.Flow // Import Flow

@Dao // Marks this as a Data Access Object interface
interface FavoritesDao {

    // Observe all favorites, ordered by when they were added (newest first)
    @Query("SELECT * FROM favorites ORDER BY addedTimestamp DESC")
    fun getAllFavorites(): Flow<List<FavoriteWallpaperEntity>>

    // Observe a single favorite by its ID
    @Query("SELECT * FROM favorites WHERE id = :id")
    fun getFavoriteById(id: String): Flow<FavoriteWallpaperEntity?>

    // *** ADD THIS FUNCTION ***
    // Get a single favorite by ID (non-Flow version for suspend calls)
    @Query("SELECT * FROM favorites WHERE id = :id LIMIT 1") // Added LIMIT 1
    suspend fun getFavoriteByIdNow(id: String): FavoriteWallpaperEntity?
    // ***********************

    // Insert a favorite. If a favorite with the same ID already exists, replace it.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(favorite: FavoriteWallpaperEntity)

    // Delete a favorite by its ID
    @Query("DELETE FROM favorites WHERE id = :id")
    suspend fun deleteById(id: String)
}
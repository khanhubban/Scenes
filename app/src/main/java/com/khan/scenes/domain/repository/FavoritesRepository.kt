package com.khan.scenes.domain.repository // Adjust package if needed

import com.khan.scenes.domain.model.Wallpaper
import kotlinx.coroutines.flow.Flow

interface FavoritesRepository {
    // Observe the list of all favorite wallpapers
    fun getFavoritesFlow(): Flow<List<Wallpaper>>

    // Add a wallpaper to favorites
    suspend fun addFavorite(wallpaper: Wallpaper)

    // Remove a wallpaper from favorites using its ID
    suspend fun removeFavorite(wallpaperId: String)

    // Observe whether a specific wallpaper is a favorite
    fun isFavoriteFlow(wallpaperId: String): Flow<Boolean>
}
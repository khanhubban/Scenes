package com.khan.scenes.domain.repository

import com.khan.scenes.domain.model.Wallpaper
import kotlinx.coroutines.flow.Flow

interface WallpaperRepository {
    // Function returning simple list flow (can be adapted for Paging 3 later)
    fun getWallpapersFlow(page: Int, perPage: Int): Flow<List<Wallpaper>>

    // Function for searching (can be adapted for Paging 3 later)
    fun searchWallpapersFlow(query: String, page: Int, perPage: Int): Flow<List<Wallpaper>>

    // Function to get details for a single wallpaper
    fun getWallpaperDetails(id: String): Flow<Wallpaper?> // Return nullable or handle error states

    // Fixed: Just the method declaration without implementation body
    suspend fun toggleFavorite(wallpaperId: String): Boolean
}
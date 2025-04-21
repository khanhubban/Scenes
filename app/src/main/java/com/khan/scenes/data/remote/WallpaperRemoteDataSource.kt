package com.khan.scenes.data.remote // Adjust package name to match your file location

import com.khan.scenes.data.remote.dto.SearchResponseDto
import com.khan.scenes.data.remote.dto.WallpaperDto

interface WallpaperRemoteDataSource {
    // Suspend functions for network calls
    suspend fun getWallpapers(page: Int, perPage: Int): List<WallpaperDto>
    suspend fun searchWallpapers(query: String, page: Int, perPage: Int): SearchResponseDto

    // *** ADD THIS FUNCTION SIGNATURE ***
    // Function to get details for a single wallpaper by ID
    // Returns nullable DTO to handle cases like 404 Not Found gracefully
    suspend fun getWallpaperDetails(id: String): WallpaperDto?
    // **********************************
}

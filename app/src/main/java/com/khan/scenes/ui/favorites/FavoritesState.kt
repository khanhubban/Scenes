// Create file: scenes/ui/favorites/FavoritesState.kt
package com.khan.scenes.ui.favorites // Create this package

import androidx.compose.runtime.Immutable
import com.khan.scenes.domain.model.Wallpaper

// Defines the possible states for the Favorites UI
@Immutable // Good practice for state objects
sealed interface FavoritesUiState {
    data object Loading : FavoritesUiState // Loading state
    data class Success(
        val favorites: List<Wallpaper> // List of favorite wallpapers
    ) : FavoritesUiState
    // Optional: Add an Error state if needed
    // data class Error(val message: String) : FavoritesUiState
}
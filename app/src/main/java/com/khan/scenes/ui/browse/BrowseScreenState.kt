package com.khan.scenes.ui.browse

import com.khan.scenes.domain.model.Wallpaper

// Represents the different states the BrowseScreen can be in
sealed interface BrowseScreenState {
    // Use 'data object' for objects without parameters in Kotlin 1.9+
    data object Loading : BrowseScreenState

    data class Success(
        val wallpapers: List<Wallpaper>,
        val query: String? = null, // Current search query, null if not searching
        val canLoadMore: Boolean = true // Flag indicating if more pages might exist
    ) : BrowseScreenState

    data class Error(val message: String) : BrowseScreenState
}
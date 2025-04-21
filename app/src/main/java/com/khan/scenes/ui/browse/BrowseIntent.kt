package com.khan.scenes.ui.browse // Adjust package if needed

// Represents user actions or events triggering state changes
sealed interface BrowseIntent {
    data object LoadInitialWallpapers : BrowseIntent // Intent to load the first page/clear search
    data object LoadNextPage : BrowseIntent          // Intent to load the next page (pagination)
    data class ToggleFavorite(val wallpaperId: String) : BrowseIntent // Intent to toggle favorite status

    // --- NEW/MODIFIED Intents ---
    // Renamed SearchWallpapers to TriggerSearch to differentiate from typing
    data object TriggerSearch : BrowseIntent             // User explicitly triggers search (e.g., presses search icon)
    data class SearchQueryChanged(val query: String) : BrowseIntent // User is typing in the search field
    data class CategorySelected(val category: String?) : BrowseIntent // User selects a category chip (null to clear)
    // --- END ---
}
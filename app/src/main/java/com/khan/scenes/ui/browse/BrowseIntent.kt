package com.khan.scenes.ui.browse // Adjust package if needed

// Represents user actions or events triggering state changes
sealed interface BrowseIntent {
    object LoadInitialWallpapers : BrowseIntent // Intent to load the first page/clear search
    object LoadNextPage : BrowseIntent          // Intent to load the next page (pagination)
    data class ToggleFavorite(val wallpaperId: String) : BrowseIntent // Intent to toggle favorite status
    data class SearchWallpapers(val query: String) : BrowseIntent // Intent to perform a search
    object ClearSearch : BrowseIntent          // Intent to clear search results
}

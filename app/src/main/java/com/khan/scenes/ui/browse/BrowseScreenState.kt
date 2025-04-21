package com.khan.scenes.ui.browse

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import com.khan.scenes.domain.model.Wallpaper // Assuming Wallpaper is stable/immutable

@Stable // Mark sealed interface as Stable
sealed interface BrowseScreenState {
    @Stable // Mark object as Stable
    data object Loading : BrowseScreenState

    // Data class with stable val properties is usually stable, but explicit annotation is clear
    @Stable
    data class Success(
        val wallpapers: List<Wallpaper>, // List is stable if Wallpaper is stable
        val query: String = "",
        val selectedCategory: String? = null,
        val isLoadingMore: Boolean = false,
        val canLoadMore: Boolean = true
    ) : BrowseScreenState

    @Stable
    data class Error(val message: String) : BrowseScreenState
}
package com.khan.scenes.ui.browse // Adjust package if needed

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.khan.scenes.domain.model.Wallpaper // Import domain model
import com.khan.scenes.domain.repository.FavoritesRepository // Import FavoritesRepository
import com.khan.scenes.domain.repository.WallpaperRepository // Import WallpaperRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.NoSuchElementException // Import for .first() exception
import javax.inject.Inject


@HiltViewModel
class BrowseViewModel @Inject constructor(
    private val wallpaperRepository: WallpaperRepository,
    private val favoritesRepository: FavoritesRepository // Inject FavoritesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<BrowseScreenState>(BrowseScreenState.Loading)
    val uiState: StateFlow<BrowseScreenState> = _uiState.asStateFlow()

    // Internal state for pagination and search query
    private var currentPage = 1
    private var currentQuery: String? = null
    private var isLoadingNextPage = false // Prevent multiple simultaneous loads

    fun processIntent(intent: BrowseIntent) {
        // Don't process pagination if already loading next page
        if (intent is BrowseIntent.LoadNextPage && isLoadingNextPage) return

        viewModelScope.launch { // Launch coroutine in ViewModel's scope
            when (intent) {
                is BrowseIntent.LoadInitialWallpapers -> {
                    currentPage = 1
                    currentQuery = null
                    loadWallpapers(page = currentPage, query = currentQuery)
                }
                is BrowseIntent.LoadNextPage -> {
                    val currentState = _uiState.value
                    // Only load next page if currently in Success state and can load more
                    if (currentState is BrowseScreenState.Success && currentState.canLoadMore) {
                        isLoadingNextPage = true // Set loading flag
                        currentPage++
                        loadWallpapers(page = currentPage, query = currentQuery)
                    }
                }
                is BrowseIntent.ToggleFavorite -> toggleFavorite(intent.wallpaperId)
                is BrowseIntent.SearchWallpapers -> {
                    val query = intent.query.trim()
                    // Handle empty query case - treat as clearing search
                    if (query.isBlank()) {
                        processIntent(BrowseIntent.ClearSearch)
                    } else {
                        currentPage = 1 // Reset page for new search
                        currentQuery = query
                        loadWallpapers(page = currentPage, query = currentQuery)
                    }
                }
                is BrowseIntent.ClearSearch -> {
                    currentPage = 1
                    currentQuery = null
                    loadWallpapers(page = currentPage, query = currentQuery)
                }
            }
        }
    }

    // Combined load/search function
    private suspend fun loadWallpapers(page: Int, query: String? = null) {
        // Set Loading state only for the first page of initial load or search
        if (page == 1) {
            _uiState.value = BrowseScreenState.Loading
        }
        // Optional: Update state to indicate loading more if page > 1

        Log.d("BrowseViewModel", "Loading page $page for query: $query")
        try {
            // Choose repository flow based on whether there's a query
            val wallpaperFlow = if (query.isNullOrBlank()) {
                wallpaperRepository.getWallpapersFlow(page, 30) // Example perPage
            } else {
                wallpaperRepository.searchWallpapersFlow(query, page, 30) // Example perPage
            }

            // Using first() assumes the flow emits the list once per request and completes
            val newWallpapers = wallpaperFlow.first()
            // Basic check: assume we can't load more if the current request returned empty
            val canLoadMore = newWallpapers.isNotEmpty()

            val currentSuccessState = _uiState.value as? BrowseScreenState.Success
            val currentWallpapers = currentSuccessState?.wallpapers ?: emptyList()

            _uiState.value = BrowseScreenState.Success(
                // Append if loading page > 1, otherwise replace
                wallpapers = if (page > 1) currentWallpapers + newWallpapers else newWallpapers,
                query = query, // Pass current query to state
                canLoadMore = canLoadMore // Pass flag to state
            )
        } catch (e: NoSuchElementException) {
            // Handle case where .first() finds no emission (empty flow from repo)
            Log.w("BrowseViewModel", "No emission from repository flow for page $page, query: $query")
            val currentSuccessState = _uiState.value as? BrowseScreenState.Success
            val currentWallpapers = currentSuccessState?.wallpapers ?: emptyList()
            if (page == 1) {
                // If first page is empty, show empty success state
                _uiState.value = BrowseScreenState.Success(emptyList(), query = query, canLoadMore = false)
            } else {
                // If subsequent page is empty, update state but signal no more pages
                _uiState.value = BrowseScreenState.Success(currentWallpapers, query = query, canLoadMore = false)
            }
        } catch (e: Exception) {
            Log.e("BrowseViewModel", "Error loading wallpapers: ${e.localizedMessage}", e)
            // Keep previous data on error if loading next page? Or show full error?
            _uiState.value = BrowseScreenState.Error(e.localizedMessage ?: "Unknown error")
        } finally {
            if (page > 1) {
                isLoadingNextPage = false // Reset loading flag for pagination
            }
        }
    }


    // Uses FavoritesRepository now
    private suspend fun toggleFavorite(wallpaperId: String) {
        val currentState = _uiState.value
        // Only proceed if we have a current list of wallpapers
        if (currentState is BrowseScreenState.Success) {
            val wallpaperToToggle = currentState.wallpapers.firstOrNull { it.id == wallpaperId } ?: return // Find the item

            // Perform repository action in the background
            try {
                if (wallpaperToToggle.isFavorite) {
                    favoritesRepository.removeFavorite(wallpaperId)
                    Log.d("BrowseViewModel", "Removed favorite $wallpaperId")
                } else {
                    // FavoritesRepo takes the domain model directly for add.
                    favoritesRepository.addFavorite(wallpaperToToggle)
                    Log.d("BrowseViewModel", "Added favorite $wallpaperId")
                }

                // Optimistic UI Update: Immediately reflect the change in the UI state
                val updatedWallpapers = currentState.wallpapers.map {
                    if (it.id == wallpaperId) {
                        it.copy(isFavorite = !it.isFavorite) // Toggle the flag
                    } else {
                        it
                    }
                }
                // Emit the updated success state with the same query/canLoadMore flags
                _uiState.value = currentState.copy(wallpapers = updatedWallpapers)

            } catch (e: Exception) {
                Log.e("BrowseViewModel", "Error toggling favorite $wallpaperId: ${e.localizedMessage}", e)
                // Optional: Show error via a UiEffect (Snackbar/Toast)
                // For now, we just log, UI state remains unchanged on error
            }
        } else {
            Log.w("BrowseViewModel", "Cannot toggle favorite, current state is not Success: $currentState")
        }
    }
}
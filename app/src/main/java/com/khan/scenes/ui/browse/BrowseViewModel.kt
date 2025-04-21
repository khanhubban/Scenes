package com.khan.scenes.ui.browse

import android.util.Log // Import Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.khan.scenes.domain.model.Wallpaper
import com.khan.scenes.domain.repository.FavoritesRepository // Import FavoritesRepository
import com.khan.scenes.domain.repository.WallpaperRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

// Define a log tag
private const val TAG = "BrowseViewModel"
private const val ITEMS_PER_PAGE = 20 // Define items per page

@HiltViewModel
class BrowseViewModel @Inject constructor(
    private val wallpaperRepository: WallpaperRepository,
    private val favoritesRepository: FavoritesRepository // Inject FavoritesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<BrowseScreenState>(BrowseScreenState.Loading)
    val uiState: StateFlow<BrowseScreenState> = _uiState.asStateFlow()

    // --- State for pagination and search ---
    private var currentPage = 1
    private var currentQuery = ""
    private var currentCategory: String? = null
    private var currentLoadingJob: Job? = null // To manage ongoing loads

    val categories = listOf("Nature", "Abstract", "Minimal", "Dark", "Colorful")

    init {
        // Load initial data when ViewModel is created
        loadWallpapers(isInitialLoad = true)
    }

    fun processIntent(intent: BrowseIntent) {
        Log.d(TAG, "Processing intent: $intent")
        when (intent) {
            is BrowseIntent.LoadInitialWallpapers -> {
                loadWallpapers(isInitialLoad = true)
            }
            is BrowseIntent.LoadNextPage -> {
                loadWallpapers(isInitialLoad = false)
            }
            is BrowseIntent.ToggleFavorite -> toggleFavorite(intent.wallpaperId) // Call updated toggleFavorite
            is BrowseIntent.TriggerSearch -> {
                currentPage = 1
                loadWallpapers(isInitialLoad = true)
            }
            is BrowseIntent.SearchQueryChanged -> {
                currentQuery = intent.query
                _uiState.update {
                    if (it is BrowseScreenState.Success) {
                        it.copy(query = currentQuery)
                    } else {
                        BrowseScreenState.Success( // Default state if not success
                            wallpapers = emptyList(),
                            query = currentQuery,
                            selectedCategory = currentCategory
                        )
                    }
                }
            }
            is BrowseIntent.CategorySelected -> {
                if (currentCategory != intent.category) {
                    currentCategory = intent.category
                    currentPage = 1
                    loadWallpapers(isInitialLoad = true)
                }
            }
        }
    }

    private fun loadWallpapers(isInitialLoad: Boolean) {
        currentLoadingJob?.cancel()
        currentLoadingJob = viewModelScope.launch {
            if (isInitialLoad) {
                currentPage = 1
                _uiState.value = BrowseScreenState.Loading
                Log.d(TAG, "Starting initial load/search. Query: '$currentQuery', Category: $currentCategory")
            } else {
                val currentState = _uiState.value
                if (currentState is BrowseScreenState.Success) {
                    if (!currentState.canLoadMore || currentState.isLoadingMore) {
                        Log.d(TAG, "Load more skipped: canLoadMore=${currentState.canLoadMore}, isLoadingMore=${currentState.isLoadingMore}")
                        return@launch
                    }
                    _uiState.value = currentState.copy(isLoadingMore = true)
                    Log.d(TAG, "Starting load more. Current Page: $currentPage, Query: '$currentQuery', Category: $currentCategory")
                } else {
                    Log.w(TAG, "Load more requested but not in Success state. State: $currentState")
                    return@launch
                }
            }

            val queryOrCategory = currentQuery.ifBlank { currentCategory ?: "" }

            val wallpaperFlow = if (queryOrCategory.isBlank()) {
                Log.d(TAG, "Calling getWallpapersFlow (Page: $currentPage)")
                wallpaperRepository.getWallpapersFlow(page = currentPage, perPage = ITEMS_PER_PAGE)
            } else {
                Log.d(TAG, "Calling searchWallpapersFlow (Query: '$queryOrCategory', Page: $currentPage)")
                wallpaperRepository.searchWallpapersFlow(
                    query = queryOrCategory,
                    page = currentPage,
                    perPage = ITEMS_PER_PAGE
                )
            }

            wallpaperFlow
                .catch { e ->
                    Log.e(TAG, "Error loading wallpapers: ${e.message}", e)
                    _uiState.value = BrowseScreenState.Error(e.localizedMessage ?: "Failed to load wallpapers")
                }
                .collect { newWallpapers ->
                    Log.d(TAG, "Received ${newWallpapers.size} wallpapers from repository.")
                    val currentList = if (isInitialLoad) emptyList() else (_uiState.value as? BrowseScreenState.Success)?.wallpapers ?: emptyList()
                    val combinedList = if (isInitialLoad) newWallpapers else (currentList + newWallpapers).distinctBy { it.id }
                    val canLoadMore = newWallpapers.size >= ITEMS_PER_PAGE

                    _uiState.value = BrowseScreenState.Success(
                        wallpapers = combinedList,
                        query = currentQuery,
                        selectedCategory = currentCategory,
                        isLoadingMore = false,
                        canLoadMore = canLoadMore
                    )

                    if (newWallpapers.isNotEmpty() || isInitialLoad) {
                        if(newWallpapers.isNotEmpty()) currentPage++
                    }
                    Log.d(TAG, "Load successful. New total: ${combinedList.size}, Can load more: $canLoadMore, Next page: $currentPage")
                }
        }
    }

    // Updated toggleFavorite logic using FavoritesRepository
    private fun toggleFavorite(wallpaperId: String) {
        viewModelScope.launch {
            try {
                // 1. Find the wallpaper in the current list or fetch details
                var wallpaper = (_uiState.value as? BrowseScreenState.Success)
                    ?.wallpapers?.find { it.id == wallpaperId }
                var wasFavorite = wallpaper?.isFavorite // Remember original state if found

                if (wallpaper == null) {
                    Log.d(TAG, "Wallpaper $wallpaperId not in current list, fetching details...")
                    wallpaper = wallpaperRepository.getWallpaperDetails(wallpaperId).firstOrNull()
                    wasFavorite = wallpaper?.isFavorite // Get state after fetching
                }

                if (wallpaper != null && wasFavorite != null) {
                    // Optimistic UI update
                    updateFavoriteStatusInState(wallpaperId, !wasFavorite)

                    // 2. Call appropriate FavoritesRepository method
                    if (wasFavorite) {
                        favoritesRepository.removeFavorite(wallpaperId)
                        Log.d(TAG, "Removed favorite $wallpaperId")
                    } else {
                        // Pass the full object (ensure isFavorite=false if repo expects clean obj)
                        favoritesRepository.addFavorite(wallpaper.copy(isFavorite = false))
                        Log.d(TAG, "Added favorite $wallpaperId")
                    }
                } else {
                    Log.w(TAG, "Could not find or fetch wallpaper details for ID: $wallpaperId to toggle favorite.")
                    // TODO: Optionally show an error message via a side-effect Flow
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error toggling favorite for $wallpaperId: ${e.message}", e)
                // TODO: Handle error (e.g., revert optimistic update, show message)
            }
        }
    }

    // Helper function for optimistic update in BrowseViewModel
    private fun updateFavoriteStatusInState(wallpaperId: String, newFavoriteState: Boolean) {
        _uiState.update { currentState ->
            if (currentState is BrowseScreenState.Success) {
                val updatedWallpapers = currentState.wallpapers.map {
                    if (it.id == wallpaperId) it.copy(isFavorite = newFavoriteState) else it
                }
                currentState.copy(wallpapers = updatedWallpapers)
            } else {
                currentState
            }
        }
    }
}
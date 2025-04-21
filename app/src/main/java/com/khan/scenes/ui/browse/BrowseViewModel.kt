package com.khan.scenes.ui.browse // Adjust package if needed

import android.util.Log // Ensure Log is imported
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.khan.scenes.domain.model.Wallpaper
import com.khan.scenes.domain.repository.FavoritesRepository
import com.khan.scenes.domain.repository.WallpaperRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.NoSuchElementException
import javax.inject.Inject

// Define a log tag for this class
private const val TAG = "BrowseViewModel"

@HiltViewModel
class BrowseViewModel @Inject constructor(
    private val wallpaperRepository: WallpaperRepository,
    private val favoritesRepository: FavoritesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<BrowseScreenState>(BrowseScreenState.Loading)
    val uiState: StateFlow<BrowseScreenState> = _uiState.asStateFlow()

    private var currentPage = 1
    private var currentQuery: String? = null
    private var isLoadingNextPage = false

    init {
        Log.d(TAG, "ViewModel initialized. Loading initial wallpapers.") // <-- ADDED LOG
        processIntent(BrowseIntent.LoadInitialWallpapers) // Trigger initial load
    }

    fun processIntent(intent: BrowseIntent) {
        Log.d(TAG, "Processing intent: ${intent::class.simpleName}") // <-- ADDED LOG

        // Prevent duplicate pagination requests
        if (intent is BrowseIntent.LoadNextPage) {
            if (isLoadingNextPage) {
                Log.d(TAG, "Intent: LoadNextPage ignored, already loading.") // <-- ADDED LOG
                return
            }
            val currentState = _uiState.value
            if (currentState !is BrowseScreenState.Success || !currentState.canLoadMore) {
                Log.d(TAG, "Intent: LoadNextPage ignored, not in Success state or cannot load more.") // <-- ADDED LOG
                return
            }
            isLoadingNextPage = true // Set loading flag
            currentPage++
            Log.d(TAG, "Intent: LoadNextPage - Incrementing page to $currentPage") // <-- ADDED LOG
            // Proceed to launch coroutine below
        }

        viewModelScope.launch { // Launch coroutine in ViewModel's scope
            when (intent) {
                is BrowseIntent.LoadInitialWallpapers -> {
                    Log.d(TAG, "Intent: LoadInitialWallpapers - Resetting state.") // <-- ADDED LOG
                    currentPage = 1
                    currentQuery = null
                    isLoadingNextPage = false // Ensure flag is reset
                    loadWallpapers(page = currentPage, query = currentQuery, isInitialLoad = true)
                }
                is BrowseIntent.LoadNextPage -> {
                    // Page incremented and flag set above
                    loadWallpapers(page = currentPage, query = currentQuery, isInitialLoad = false)
                }
                is BrowseIntent.ToggleFavorite -> toggleFavorite(intent.wallpaperId) // Keep existing logic
                is BrowseIntent.SearchWallpapers -> {
                    val query = intent.query.trim()
                    if (query.isBlank()) {
                        Log.d(TAG, "Intent: SearchWallpapers - Query is blank, clearing search.") // <-- ADDED LOG
                        processIntent(BrowseIntent.ClearSearch) // Treat blank search as clear
                    } else {
                        Log.d(TAG, "Intent: SearchWallpapers - Starting search for '$query'") // <-- ADDED LOG
                        currentPage = 1
                        currentQuery = query
                        isLoadingNextPage = false // Ensure flag is reset
                        loadWallpapers(page = currentPage, query = currentQuery, isInitialLoad = true)
                    }
                }
                is BrowseIntent.ClearSearch -> {
                    Log.d(TAG, "Intent: ClearSearch - Resetting state.") // <-- ADDED LOG
                    currentPage = 1
                    currentQuery = null
                    isLoadingNextPage = false // Ensure flag is reset
                    loadWallpapers(page = currentPage, query = currentQuery, isInitialLoad = true)
                }
            }
        }
    }

    // Added isInitialLoad parameter to control Loading state emission
    private suspend fun loadWallpapers(page: Int, query: String?, isInitialLoad: Boolean) {
        Log.d(TAG, "loadWallpapers - page: $page, query: $query, isInitialLoad: $isInitialLoad") // <-- ADDED LOG

        // Set Loading state ONLY for the very first load or the start of a new search
        if (isInitialLoad) {
            Log.d(TAG, "loadWallpapers - Setting state to Loading") // <-- ADDED LOG
            _uiState.value = BrowseScreenState.Loading
        }
        // Optional: Indicate loading more state if page > 1 ? (Could add a flag to Success state)

        try {
            Log.d(TAG, "loadWallpapers - Calling repository flow...") // <-- ADDED LOG
            val wallpaperFlow = if (query.isNullOrBlank()) {
                wallpaperRepository.getWallpapersFlow(page, 30)
            } else {
                wallpaperRepository.searchWallpapersFlow(query, page, 30)
            }

            // Using first() - be mindful if the repository flow could error *before* emitting.
            // The repository now re-throws, so the catch block here should handle it.
            Log.d(TAG, "loadWallpapers - Collecting first emission from repository flow...") // <-- ADDED LOG
            val newWallpapers = wallpaperFlow.first()
            Log.d(TAG, "loadWallpapers - Received ${newWallpapers.size} items from repository flow.") // <-- ADDED LOG

            val canLoadMore = newWallpapers.isNotEmpty() // Simple check

            val currentSuccessState = _uiState.value as? BrowseScreenState.Success
            // If it's the initial load OR a new search, currentWallpapers should be empty.
            // If it's pagination (page > 1), use the existing list.
            val existingWallpapers = if (page > 1) currentSuccessState?.wallpapers ?: emptyList() else emptyList()

            val newState = BrowseScreenState.Success(
                wallpapers = existingWallpapers + newWallpapers,
                query = query,
                canLoadMore = canLoadMore
            )
            Log.d(TAG, "loadWallpapers - Setting state to Success (Wallpapers: ${newState.wallpapers.size}, CanLoadMore: $canLoadMore)") // <-- ADDED LOG
            _uiState.value = newState

        } catch (e: NoSuchElementException) {
            // This catches if the flow completes *without* emitting (e.g., repo returns empty flow)
            Log.w(TAG, "loadWallpapers - Flow completed without emitting for page $page, query: $query", e) // <-- MODIFIED LOG
            val currentSuccessState = _uiState.value as? BrowseScreenState.Success
            val existingWallpapers = if (page > 1) currentSuccessState?.wallpapers ?: emptyList() else emptyList()

            // If it was the first page, show empty success. If pagination, just stop loading more.
            val newState = BrowseScreenState.Success(
                wallpapers = existingWallpapers, // Keep existing wallpapers if paginating
                query = query,
                canLoadMore = false // Can't load more if flow was empty
            )
            Log.d(TAG, "loadWallpapers - Setting state to Success (Flow Empty) (Wallpapers: ${newState.wallpapers.size}, CanLoadMore: ${newState.canLoadMore})") // <-- ADDED LOG
            _uiState.value = newState

        } catch (e: Exception) {
            // Catch errors propagated from the repository (or other unexpected errors)
            Log.e(TAG, "loadWallpapers - Error loading wallpapers: ${e.localizedMessage}", e) // <-- MODIFIED LOG
            val errorMessage = e.localizedMessage ?: "Unknown error occurred"
            Log.d(TAG, "loadWallpapers - Setting state to Error: $errorMessage") // <-- ADDED LOG
            // Keep previous data on pagination error? For now, just show error state.
            _uiState.value = BrowseScreenState.Error(errorMessage)
        } finally {
            if (page > 1) {
                Log.d(TAG, "loadWallpapers - Resetting isLoadingNextPage flag.") // <-- ADDED LOG
                isLoadingNextPage = false // Reset pagination flag
            }
        }
    }


    // Keep existing toggleFavorite implementation (or add logging if needed)
    private suspend fun toggleFavorite(wallpaperId: String) {
        Log.d(TAG, "toggleFavorite: Received toggle request for ID $wallpaperId")
        val currentState = _uiState.value
        if (currentState is BrowseScreenState.Success) {
            val wallpaperToToggle = currentState.wallpapers.firstOrNull { it.id == wallpaperId }
            if (wallpaperToToggle == null) {
                Log.w(TAG, "toggleFavorite: Wallpaper ID $wallpaperId not found in current state.")
                return
            }

            // Optimistic UI update first
            val updatedWallpapers = currentState.wallpapers.map {
                if (it.id == wallpaperId) it.copy(isFavorite = !it.isFavorite) else it
            }
            Log.d(TAG, "toggleFavorite: Applying optimistic UI update for $wallpaperId")
            // Keep query and canLoadMore flags from the current state
            _uiState.value = currentState.copy(wallpapers = updatedWallpapers)

            // Then call repository
            try {
                if (wallpaperToToggle.isFavorite) { // If it *was* favorite before toggle
                    Log.d(TAG, "toggleFavorite: Calling repository removeFavorite for $wallpaperId")
                    favoritesRepository.removeFavorite(wallpaperId)
                } else {
                    Log.d(TAG, "toggleFavorite: Calling repository addFavorite for $wallpaperId")
                    // Pass the original wallpaper object (before toggle) to addFavorite
                    favoritesRepository.addFavorite(wallpaperToToggle)
                }
                Log.d(TAG, "toggleFavorite: Repository call successful for $wallpaperId")
            } catch (e: Exception) {
                Log.e(TAG, "toggleFavorite: Error calling repository for $wallpaperId: ${e.localizedMessage}", e)
                // Revert optimistic update on error
                Log.d(TAG, "toggleFavorite: Reverting optimistic UI update due to error for $wallpaperId")
                _uiState.value = currentState
                // Optional: Show error message via a single-event Flow (SharedFlow/Channel)
            }
        } else {
            Log.w(TAG, "toggleFavorite: Cannot toggle favorite, not in Success state.")
        }
    }
}
package com.khan.scenes.ui.detail // Adjust package if needed

import android.util.Log // Import Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.khan.scenes.domain.model.Wallpaper
import com.khan.scenes.domain.repository.FavoritesRepository // *** Import FavoritesRepository ***
import com.khan.scenes.domain.repository.WallpaperRepository
import com.khan.scenes.ui.navigation.AppDestinations
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

// (DetailScreenState remains the same)
sealed interface DetailScreenState {
    object Loading : DetailScreenState
    data class Success(val wallpaper: Wallpaper) : DetailScreenState
    data class Error(val message: String) : DetailScreenState
}

@HiltViewModel
class WallpaperDetailViewModel @Inject constructor(
    private val wallpaperRepository: WallpaperRepository,
    private val favoritesRepository: FavoritesRepository, // *** Inject FavoritesRepository ***
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val wallpaperId: String = savedStateHandle.get<String>(AppDestinations.WALLPAPER_ID_ARG)
        ?: throw IllegalStateException("Wallpaper ID not found in navigation arguments")

    private val _uiState = MutableStateFlow<DetailScreenState>(DetailScreenState.Loading)
    val uiState: StateFlow<DetailScreenState> = _uiState.asStateFlow()

    init {
        loadDetails()
    }

    // *** Add processIntent function ***
    fun processIntent(intent: DetailIntent) {
        viewModelScope.launch {
            when (intent) {
                is DetailIntent.ToggleFavorite -> toggleFavorite()
            }
        }
    }

    private fun loadDetails() {
        viewModelScope.launch {
            _uiState.value = DetailScreenState.Loading
            wallpaperRepository.getWallpaperDetails(wallpaperId)
                .catch { e ->
                    Log.e("DetailVM", "Error loading details: ${e.localizedMessage}", e)
                    _uiState.value = DetailScreenState.Error(e.localizedMessage ?: "Unknown error fetching details")
                }
                .collect { wallpaper ->
                    if (wallpaper != null) {
                        _uiState.value = DetailScreenState.Success(wallpaper)
                    } else {
                        Log.w("DetailVM", "Wallpaper details came back null for ID: $wallpaperId")
                        _uiState.value = DetailScreenState.Error("Wallpaper not found")
                    }
                }
        }
    }

    // *** Add toggleFavorite function ***
    private suspend fun toggleFavorite() {
        val currentState = _uiState.value
        if (currentState is DetailScreenState.Success) {
            val wallpaper = currentState.wallpaper
            try {
                // Optimistic UI Update: Toggle state immediately
                _uiState.value = DetailScreenState.Success(wallpaper.copy(isFavorite = !wallpaper.isFavorite))

                // Perform repository action
                if (wallpaper.isFavorite) { // If it *was* favorite before toggle
                    favoritesRepository.removeFavorite(wallpaper.id)
                    Log.d("DetailVM", "Removed favorite ${wallpaper.id}")
                } else { // If it *was not* favorite before toggle
                    // Important: Pass the *original* wallpaper object before toggle to add
                    favoritesRepository.addFavorite(wallpaper)
                    Log.d("DetailVM", "Added favorite ${wallpaper.id}")
                }
                // Note: We don't necessarily need to reload details after toggle,
                // as the favorite status is the only thing changing, and we updated it optimistically.
                // If repository calls fail, the optimistic update might be wrong,
                // consider adding error handling feedback (e.g., SnackBar) if needed.

            } catch (e: Exception) {
                Log.e("DetailVM", "Error toggling favorite ${wallpaper.id}: ${e.localizedMessage}", e)
                // Revert optimistic update on error
                _uiState.value = currentState // Put state back
                // TODO: Show error message to user (e.g., SnackBar)
            }
        } else {
            Log.w("DetailVM", "Cannot toggle favorite, not in Success state.")
        }
    }
}
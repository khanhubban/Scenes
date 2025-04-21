// Create file: scenes/ui/favorites/FavoritesViewModel.kt
package com.khan.scenes.ui.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.khan.scenes.domain.repository.FavoritesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val favoritesRepository: FavoritesRepository
) : ViewModel() {

    // Observe the favorites flow from the repository and map it to the UI state
    val uiState: StateFlow<FavoritesUiState> = favoritesRepository.getFavoritesFlow()
        .map { favoritesList ->
            FavoritesUiState.Success(favoritesList) // Map the list to the Success state
        }
        .stateIn(
            scope = viewModelScope,
            // Start eagerly to load favorites immediately
            started = SharingStarted.Eagerly,
            // Initial state while waiting for the first emission from the flow
            initialValue = FavoritesUiState.Loading
        )

    // TODO: Add functions here if users can interact with favorites on this screen
    // e.g., remove favorite directly from this screen?
    // fun removeFavorite(wallpaperId: String) { ... }
}
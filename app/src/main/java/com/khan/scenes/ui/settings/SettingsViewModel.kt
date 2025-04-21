package com.khan.scenes.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.khan.scenes.domain.repository.SettingsRepository // Ensure this import is correct
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Data class holding the state for the Settings Screen UI.
 * Add properties here for each setting defined in SettingsRepository.
 */
data class SettingsUiState(
    val isDarkThemeEnabled: Boolean = false, // Changed from selectedTheme
    val isLoading: Boolean = true,
    val autoChangeInterval: String = "Never", // Add defaults based on repository logic
    val autoChangeSource: String = "None"     // Add defaults based on repository logic
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository // Hilt injects the repository
) : ViewModel() {

    // Private MutableStateFlow to hold the UI state internally
    private val _uiState = MutableStateFlow(SettingsUiState(isLoading = true))
    // Public StateFlow exposed to the UI
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            // Combine multiple flows from the repository to load all settings
            combine(
                settingsRepository.darkThemeEnabledFlow,
                settingsRepository.autoChangeIntervalFlow,
                settingsRepository.autoChangeSourceFlow
            ) { isDarkTheme, interval, source ->
                // When any setting flow emits, update the entire state
                SettingsUiState(
                    isDarkThemeEnabled = isDarkTheme,
                    isLoading = false, // Mark loading complete once first combined emission happens
                    autoChangeInterval = interval,
                    autoChangeSource = source
                )
            }.catch { e ->
                // Handle potential errors during loading (e.g., log it)
                // Emit a default state or an error state
                _uiState.update {
                    it.copy(isLoading = false /* consider adding an error message */)
                }
                println("Error loading settings: ${e.message}") // Basic logging
            }.collect { combinedState ->
                // Update the UI state with the latest combined settings
                _uiState.value = combinedState
            }
        }
    }

    /**
     * Updates the dark theme setting in the repository.
     * The UI state will update automatically because we are collecting the combined flows.
     *
     * @param isEnabled The new value for the dark theme setting.
     */
    fun updateDarkTheme(isEnabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setDarkThemeEnabled(isEnabled)
        }
    }

    /**
     * Updates the auto-change interval setting.
     */
    fun updateAutoChangeInterval(interval: String) {
        viewModelScope.launch {
            settingsRepository.setAutoChangeInterval(interval)
        }
    }

    /**
     * Updates the auto-change source setting.
     */
    fun updateAutoChangeSource(source: String) {
        viewModelScope.launch {
            settingsRepository.setAutoChangeSource(source)
        }
    }
}
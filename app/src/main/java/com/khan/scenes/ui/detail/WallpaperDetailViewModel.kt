package com.khan.scenes.ui.detail // Adjust package if needed

import android.app.DownloadManager // *** Import DownloadManager ***
import android.app.WallpaperManager // *** Import WallpaperManager ***
import android.content.Context // *** Import Context ***
import android.graphics.Bitmap // *** Import Bitmap ***
import android.net.Uri // *** Import Uri ***
import android.os.Environment // *** Import Environment ***
import android.util.Log
import android.widget.Toast // *** Import Toast for simple feedback ***
import androidx.core.graphics.drawable.toBitmap // *** Import bitmap extension ***
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.imageLoader // *** Import Coil extension ***
import coil.request.ImageRequest // *** Import Coil request ***
import coil.request.SuccessResult // *** Import Coil result ***
import com.khan.scenes.domain.model.Wallpaper
import com.khan.scenes.domain.repository.FavoritesRepository
import com.khan.scenes.domain.repository.WallpaperRepository
import com.khan.scenes.ui.navigation.AppDestinations
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers // *** Import Dispatchers ***
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext // *** Import withContext ***
import java.io.IOException
import javax.inject.Inject

// (DetailScreenState remains the same)
sealed interface DetailScreenState {
    object Loading : DetailScreenState
    data class Success(val wallpaper: Wallpaper) : DetailScreenState
    data class Error(val message: String) : DetailScreenState
}

// Events for one-time UI feedback (like Toasts)
sealed interface DetailScreenEvent {
    data class ShowToast(val message: String) : DetailScreenEvent
}

@HiltViewModel
class WallpaperDetailViewModel @Inject constructor(
    private val wallpaperRepository: WallpaperRepository,
    private val favoritesRepository: FavoritesRepository,
    @ApplicationContext private val context: Context,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val wallpaperId: String = savedStateHandle.get<String>(AppDestinations.WALLPAPER_ID_ARG)
        ?: throw IllegalStateException("Wallpaper ID not found in navigation arguments")

    private val _uiState = MutableStateFlow<DetailScreenState>(DetailScreenState.Loading)
    val uiState: StateFlow<DetailScreenState> = _uiState.asStateFlow()

    // *** Add SharedFlow for events ***
    private val _uiEvents = MutableSharedFlow<DetailScreenEvent>()
    val uiEvents: SharedFlow<DetailScreenEvent> = _uiEvents.asSharedFlow()
    // **********************************

    init {
        loadDetails()
    }

    fun processIntent(intent: DetailIntent) {
        viewModelScope.launch {
            when (intent) {
                is DetailIntent.ToggleFavorite -> toggleFavorite()
                is DetailIntent.DownloadWallpaper -> downloadWallpaper()
                is DetailIntent.SetWallpaper -> setWallpaper()
            }
        }
    }

    private fun loadDetails() {
        // (Keep existing implementation)
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

    private suspend fun toggleFavorite() {
        // (Keep existing implementation)
        val currentState = _uiState.value
        if (currentState is DetailScreenState.Success) {
            val wallpaper = currentState.wallpaper
            try {
                _uiState.value = DetailScreenState.Success(wallpaper.copy(isFavorite = !wallpaper.isFavorite)) // Optimistic update
                if (wallpaper.isFavorite) {
                    favoritesRepository.removeFavorite(wallpaper.id)
                    Log.d("DetailVM", "Removed favorite ${wallpaper.id}")
                } else {
                    favoritesRepository.addFavorite(wallpaper)
                    Log.d("DetailVM", "Added favorite ${wallpaper.id}")
                }
            } catch (e: Exception) {
                Log.e("DetailVM", "Error toggling favorite ${wallpaper.id}: ${e.localizedMessage}", e)
                _uiState.value = currentState // Revert
                _uiEvents.emit(DetailScreenEvent.ShowToast("Error toggling favorite"))
            }
        } else {
            Log.w("DetailVM", "Cannot toggle favorite, not in Success state.")
        }
    }

    // *** Implement Download Logic ***
    private suspend fun downloadWallpaper() { // Mark as suspend if needed for future async ops
        val state = _uiState.value
        if (state is DetailScreenState.Success) {
            val wallpaper = state.wallpaper
            val url = wallpaper.fullUrl // Get the best quality URL
            Log.d("DetailVM", "Download started for: $url")

            try {
                val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                val request = DownloadManager.Request(Uri.parse(url))
                    .setTitle("Scenes Wallpaper (${wallpaper.id})") // Example title
                    .setDescription("Downloading")
                    .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                    // Saves to standard Downloads directory. Subdirectory "Scenes" is optional.
                    .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "Scenes/${wallpaper.id}.jpg")
                    .setAllowedOverMetered(true) // Allow download over mobile data
                    .setAllowedOverRoaming(true)

                downloadManager.enqueue(request)
                _uiEvents.emit(DetailScreenEvent.ShowToast("Download started..."))

            } catch (e: Exception) {
                Log.e("DetailVM", "Error starting download: ${e.localizedMessage}", e)
                _uiEvents.emit(DetailScreenEvent.ShowToast("Download failed to start"))
                // Handle exceptions, e.g., invalid URL, missing permission (though DownloadManager often handles this)
            }
        } else {
            Log.w("DetailVM", "Cannot download, not in Success state.")
            _uiEvents.emit(DetailScreenEvent.ShowToast("Cannot download wallpaper details"))
        }
    }
    // ******************************

    // *** Implement Set Wallpaper Logic ***
    private suspend fun setWallpaper() {
        val state = _uiState.value
        if (state is DetailScreenState.Success) {
            val wallpaper = state.wallpaper
            // Prefer regular URL for setting wallpaper to avoid excessive data/memory use,
            // but could use fullUrl if desired.
            val url = wallpaper.regularUrl
            Log.d("DetailVM", "Setting wallpaper from: $url")
            _uiEvents.emit(DetailScreenEvent.ShowToast("Setting wallpaper...")) // Initial feedback

            // Use IO Dispatcher for network and bitmap operations
            withContext(Dispatchers.IO) {
                try {
                    // 1. Fetch image using Coil
                    val request = ImageRequest.Builder(context)
                        .data(url)
                        .allowHardware(false) // Important for WallpaperManager: disable hardware bitmaps
                        .build()
                    val result = context.imageLoader.execute(request)

                    if (result is SuccessResult) {
                        // 2. Get Bitmap from Coil's result
                        val bitmap = result.drawable.toBitmap()

                        // 3. Set Wallpaper using WallpaperManager
                        val wallpaperManager = WallpaperManager.getInstance(context)
                        wallpaperManager.setBitmap(bitmap)

                        // Emit success event on the main thread (though SharedFlow is thread-safe)
                        withContext(Dispatchers.Main) {
                            Log.d("DetailVM", "Wallpaper set successfully!")
                            _uiEvents.emit(DetailScreenEvent.ShowToast("Wallpaper set!"))
                        }
                    } else {
                        throw IOException("Failed to load image with Coil")
                    }
                } catch (e: Exception) {
                    Log.e("DetailVM", "Error setting wallpaper: ${e.localizedMessage}", e)
                    // Emit error event on the main thread
                    withContext(Dispatchers.Main) {
                        _uiEvents.emit(DetailScreenEvent.ShowToast("Failed to set wallpaper"))
                    }
                }
            } // End IO Context
        } else {
            Log.w("DetailVM", "Cannot set wallpaper, not in Success state.")
            _uiEvents.emit(DetailScreenEvent.ShowToast("Cannot get wallpaper details"))
        }
    }
    // *******************************
}
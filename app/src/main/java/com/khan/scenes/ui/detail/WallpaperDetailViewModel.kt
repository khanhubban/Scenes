package com.khan.scenes.ui.detail // Adjust package if needed

import android.app.DownloadManager
import android.app.WallpaperManager
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.imageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.khan.scenes.domain.model.Wallpaper // Wallpaper model now has more fields
import com.khan.scenes.domain.repository.FavoritesRepository
import com.khan.scenes.domain.repository.WallpaperRepository
import com.khan.scenes.ui.navigation.AppDestinations
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import javax.inject.Inject

// State Definition - Success now holds the richer Wallpaper object
sealed interface DetailScreenState {
    object Loading : DetailScreenState
    data class Success(val wallpaper: Wallpaper) : DetailScreenState // Holds the updated Wallpaper
    data class Error(val message: String) : DetailScreenState
}

// Events for one-time UI feedback (like Toasts)
sealed interface DetailScreenEvent {
    data class ShowToast(val message: String) : DetailScreenEvent
}

private const val TAG = "DetailVM" // Log tag

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

    private val _uiEvents = MutableSharedFlow<DetailScreenEvent>()
    val uiEvents: SharedFlow<DetailScreenEvent> = _uiEvents.asSharedFlow()

    init {
        loadDetails()
    }

    // processIntent remains the same
    fun processIntent(intent: DetailIntent) {
        viewModelScope.launch {
            when (intent) {
                is DetailIntent.ToggleFavorite -> toggleFavorite()
                is DetailIntent.DownloadWallpaper -> downloadWallpaper()
                is DetailIntent.SetWallpaper -> setWallpaper()
            }
        }
    }

    // loadDetails remains the same - it fetches the Wallpaper object
    // which now includes the new fields thanks to repository updates
    private fun loadDetails() {
        viewModelScope.launch {
            _uiState.value = DetailScreenState.Loading
            wallpaperRepository.getWallpaperDetails(wallpaperId)
                .catch { e ->
                    Log.e(TAG, "Error loading details flow: ${e.localizedMessage}", e)
                    _uiState.value = DetailScreenState.Error(e.localizedMessage ?: "Unknown error fetching details")
                }
                .collect { wallpaper ->
                    if (wallpaper != null) {
                        _uiState.value = DetailScreenState.Success(wallpaper)
                        Log.d(TAG, "Successfully loaded details for $wallpaperId. Desc: ${wallpaper.description}, Fav: ${wallpaper.isFavorite}")
                    } else {
                        Log.w(TAG, "Wallpaper details came back null for ID: $wallpaperId")
                        _uiState.value = DetailScreenState.Error("Wallpaper not found")
                    }
                }
        }
    }

    // toggleFavorite remains the same
    private suspend fun toggleFavorite() {
        val currentState = _uiState.value
        if (currentState is DetailScreenState.Success) {
            val wallpaper = currentState.wallpaper
            try {
                _uiState.value = DetailScreenState.Success(wallpaper.copy(isFavorite = !wallpaper.isFavorite))

                if (wallpaper.isFavorite) {
                    favoritesRepository.removeFavorite(wallpaper.id)
                    Log.d(TAG, "Removed favorite ${wallpaper.id}")
                    _uiEvents.emit(DetailScreenEvent.ShowToast("Removed from favorites"))
                } else {
                    favoritesRepository.addFavorite(wallpaper)
                    Log.d(TAG, "Added favorite ${wallpaper.id}")
                    _uiEvents.emit(DetailScreenEvent.ShowToast("Added to favorites"))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error toggling favorite ${wallpaper.id}: ${e.localizedMessage}", e)
                _uiState.value = currentState
                _uiEvents.emit(DetailScreenEvent.ShowToast("Error toggling favorite"))
            }
        } else {
            Log.w(TAG, "Cannot toggle favorite, not in Success state.")
        }
    }


    // downloadWallpaper remains the same
    private suspend fun downloadWallpaper() {
        val state = _uiState.value
        if (state is DetailScreenState.Success) {
            val wallpaper = state.wallpaper
            val url = wallpaper.fullUrl
            Log.d(TAG, "Download started for: $url")

            try {
                val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                val request = DownloadManager.Request(Uri.parse(url))
                    .setTitle("Scenes Wallpaper (${wallpaper.id})")
                    .setDescription("Downloading")
                    .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                    .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "Scenes/${wallpaper.id}.jpg")
                    .setAllowedOverMetered(true)
                    .setAllowedOverRoaming(true)

                downloadManager.enqueue(request)
                _uiEvents.emit(DetailScreenEvent.ShowToast("Download started..."))

            } catch (e: Exception) {
                Log.e(TAG, "Error starting download: ${e.localizedMessage}", e)
                _uiEvents.emit(DetailScreenEvent.ShowToast("Download failed to start"))
            }
        } else {
            Log.w(TAG, "Cannot download, not in Success state.")
            _uiEvents.emit(DetailScreenEvent.ShowToast("Cannot download wallpaper details"))
        }
    }

    // setWallpaper remains the same
    private suspend fun setWallpaper() {
        val state = _uiState.value
        if (state is DetailScreenState.Success) {
            val wallpaper = state.wallpaper
            val url = wallpaper.regularUrl
            Log.d(TAG, "Setting wallpaper from: $url")
            _uiEvents.emit(DetailScreenEvent.ShowToast("Setting wallpaper..."))

            withContext(Dispatchers.IO) {
                try {
                    val request = ImageRequest.Builder(context)
                        .data(url)
                        .allowHardware(false)
                        .build()
                    val result = context.imageLoader.execute(request)

                    if (result is SuccessResult) {
                        val bitmap = result.drawable.toBitmap()
                        val wallpaperManager = WallpaperManager.getInstance(context)
                        wallpaperManager.setBitmap(bitmap)

                        withContext(Dispatchers.Main) {
                            Log.d(TAG, "Wallpaper set successfully!")
                            _uiEvents.emit(DetailScreenEvent.ShowToast("Wallpaper set!"))
                        }
                    } else {
                        throw IOException("Failed to load image with Coil")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error setting wallpaper: ${e.localizedMessage}", e)
                    withContext(Dispatchers.Main) {
                        _uiEvents.emit(DetailScreenEvent.ShowToast("Failed to set wallpaper"))
                    }
                }
            }
        } else {
            Log.w(TAG, "Cannot set wallpaper, not in Success state.")
            _uiEvents.emit(DetailScreenEvent.ShowToast("Cannot get wallpaper details"))
        }
    }
}
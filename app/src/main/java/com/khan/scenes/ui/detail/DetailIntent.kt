package com.khan.scenes.ui.detail // Adjust package if needed

// Represents user actions or events on the Detail Screen
sealed interface DetailIntent {
    object ToggleFavorite : DetailIntent // Intent to toggle favorite status of the current wallpaper
}
package com.khan.scenes.domain.model // Adjust package if needed

// Represents a Wallpaper object as needed by the UI/Domain layers
data class Wallpaper(
    val id: String,
    val smallUrl: String,
    val regularUrl: String,
    val fullUrl: String,
    val userName: String?,
    val userLink: String?,
    var isFavorite: Boolean = false // UI might need to know favorite status
)
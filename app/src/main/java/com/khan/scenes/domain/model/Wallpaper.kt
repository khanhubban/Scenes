package com.khan.scenes.domain.model

import androidx.compose.runtime.Immutable

@Immutable
data class Wallpaper(
    val id: String,
    val smallUrl: String,
    val regularUrl: String,
    val fullUrl: String,
    val thumbUrl: String, // <-- *** ENSURE THIS LINE EXISTS ***
    val userName: String?,
    val userLink: String?,
    val isFavorite: Boolean = false,
    val description: String? = null,
    val width: Int? = null,
    val height: Int? = null,
    val downloads: Int? = null,
    val tags: List<String>? = null
)

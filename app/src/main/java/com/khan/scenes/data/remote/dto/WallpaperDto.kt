package com.khan.scenes.data.remote.dto // Adjust package name to match your file location

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable // Required for Ktor serialization
data class WallpaperDto(
    @SerialName("id") val id: String, // Matches JSON key "id"
    @SerialName("urls") val urls: WallpaperUrlsDto,
    @SerialName("user") val user: UserDto? = null, // Example nested object (nullable)
    @SerialName("description") val description: String? = null,
    // Add other fields returned by your API (e.g., color, likes, width, height)
)

@Serializable
data class WallpaperUrlsDto(
    @SerialName("raw") val raw: String,
    @SerialName("full") val full: String,
    @SerialName("regular") val regular: String,
    @SerialName("small") val small: String,
    @SerialName("thumb") val thumb: String
)

@Serializable
data class UserDto(
    @SerialName("name") val name: String?,
    @SerialName("links") val links: UserLinksDto?
    // Add other user fields
)

@Serializable
data class UserLinksDto(
    @SerialName("html") val html: String?
    // Add other link fields
)

// If your API returns a list within a wrapper object (e.g., for search results)
@Serializable
data class SearchResponseDto(
    @SerialName("total") val total: Int,
    @SerialName("total_pages") val totalPages: Int,
    @SerialName("results") val results: List<WallpaperDto>
)


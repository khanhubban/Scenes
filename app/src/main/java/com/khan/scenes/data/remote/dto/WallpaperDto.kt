package com.khan.scenes.data.remote.dto // Adjust package name to match your file location

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WallpaperDto(
    @SerialName("id") val id: String,
    @SerialName("urls") val urls: WallpaperUrlsDto,
    @SerialName("user") val user: UserDto? = null,

    // --- NEW FIELDS ---
    @SerialName("description") val description: String? = null, // Often photo description or alt_description
    @SerialName("alt_description") val altDescription: String? = null, // Use this if 'description' is often null
    @SerialName("width") val width: Int? = null,
    @SerialName("height") val height: Int? = null,
    @SerialName("downloads") val downloads: Int? = null,
    @SerialName("tags") val tags: List<TagDto>? = null, // Assuming API returns tags as a list of objects
    // Add other fields if needed (e.g., exif for file size approximation, likes)
    // @SerialName("likes") val likes: Int? = null,
    // @SerialName("exif") val exif: ExifDto? = null
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
)

@Serializable
data class UserLinksDto(
    @SerialName("html") val html: String?
)

// --- NEW DTO for Tags ---
@Serializable
data class TagDto(
    @SerialName("title") val title: String?
)

// --- Optional: DTO for Exif if needed for file size ---
// @Serializable
// data class ExifDto(
//    @SerialName("make") val make: String?,
//    @SerialName("model") val model: String?,
//    ... // Other fields that might correlate to size? API often doesn't provide direct file size.
// )


// --- Search Response remains the same ---
@Serializable
data class SearchResponseDto(
    @SerialName("total") val total: Int,
    @SerialName("total_pages") val totalPages: Int,
    @SerialName("results") val results: List<WallpaperDto>
)


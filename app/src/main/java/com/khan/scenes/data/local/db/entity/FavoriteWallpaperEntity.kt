package com.khan.scenes.data.local.db.entity // Adjust package if needed

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorites") // Defines the table name
data class FavoriteWallpaperEntity(
    @PrimaryKey val id: String, // The wallpaper ID will be the primary key
    val smallUrl: String,       // URL for the small image version
    val regularUrl: String,     // URL for the regular image version
    val fullUrl: String,        // URL for the full image version
    val userName: String?,      // Photographer's name (nullable)
    val userLink: String?,      // Link to photographer's profile (nullable)
    val addedTimestamp: Long = System.currentTimeMillis() // Timestamp when added
)
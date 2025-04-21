package com.khan.scenes.data.local.datastore // Adjust package if needed

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
// Import other key types as needed (intPreferencesKey, longPreferencesKey, etc.)

object PreferencesKeys {
    // Key for enabling/disabling dark theme (Boolean type)
    val DARK_THEME_ENABLED = booleanPreferencesKey("dark_theme_enabled")

    // Key for auto-change interval (String type, e.g., "never", "1h", "6h", "24h")
    val AUTO_CHANGE_INTERVAL = stringPreferencesKey("auto_change_interval")

    // Key for the source of auto-change wallpapers (String type, e.g., "favorites", "category_nature")
    val AUTO_CHANGE_SOURCE = stringPreferencesKey("auto_change_source")

    // Add other keys for settings you might need
}
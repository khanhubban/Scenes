package com.khan.scenes.data.local.datastore // Adjust package if needed

import android.content.Context
import androidx.datastore.core.DataStore // Import DataStore
import androidx.datastore.preferences.core.Preferences // Import Preferences
import androidx.datastore.preferences.preferencesDataStore // Import delegate

// Define a constant for the DataStore file name
private const val USER_PREFERENCES_NAME = "user_settings"

// Extension property on Context to provide a singleton DataStore instance
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = USER_PREFERENCES_NAME
    // Optional: Add migrations here if needed, e.g., from SharedPreferences
    // migrations = listOf(SharedPreferencesMigration(context, OLD_SHARED_PREFS_NAME))
)
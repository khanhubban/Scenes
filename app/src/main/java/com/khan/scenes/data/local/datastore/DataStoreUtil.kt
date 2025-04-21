package com.khan.scenes.data.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore

// Define a constant for the DataStore file name
private const val USER_PREFERENCES_NAME = "user_settings"

// Extension property on Context to provide a singleton DataStore instance
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = USER_PREFERENCES_NAME

)
package com.khan.scenes // Use your base package name

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp // Required annotation for Hilt dependency injection setup
class WallpaperApplication : Application() {
    // You can add application-level setup logic here if needed in the future.
    // For example:
    // override fun onCreate() {
    //     super.onCreate()
    //     // Initialization code that needs to run once when the app starts
    // }
}

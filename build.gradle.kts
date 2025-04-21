// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    // Plugin for the Android Application module
    // Latest stable Android Application plugin (Note: 8.4.2 is a widely used stable version as of April 2025, though newer exist. Choose based on your project's needs and Android Studio compatibility)
    id("com.android.application") version "8.9.1" apply false

    // Plugin for the Android Library module (if you have library modules)
    id("com.android.library") version "8.9.1" apply false // Use the same version as com.android.application

    // Plugin for Kotlin projects targeting Android
    // Using Kotlin version 2.0.0 as specified
    id("org.jetbrains.kotlin.android") version "2.0.0" apply false

    // Plugin for Kotlin Serialization
    // The serialization plugin version should match your Kotlin version
    id("org.jetbrains.kotlin.plugin.serialization") version "2.0.0" apply false

    // Compose Compiler Gradle Plugin (recommended for Kotlin 2.0.0+)
    // The version should match your Kotlin version
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.0" apply false

    // Dagger Hilt plugin for Android
    id("com.google.dagger.hilt.android") version "2.56.1" apply false // Latest stable Hilt version

    // KSP plugin for faster annotation processing (preferred over KAPT)
    // IMPORTANT: The KSP version must align with your Kotlin version (2.0.0).
    // Find the compatible KSP version for your Kotlin version.
    // Compatible KSP version for Kotlin 2.0.0 is typically 2.0.x-1.y.z. 2.0.21-1.0.28 is a recent one.
    id("com.google.devtools.ksp") version "2.0.0-1.0.21" apply false // Example version, ensure it matches your Kotlin 2.0.0 version

    // AndroidX Room plugin
    id("androidx.room") version "2.7.0" apply false // Latest stable Room version

    // Optional: Keep KAPT only if you have dependencies that *still* require it
    // Note: KAPT is in maintenance mode. Consider migrating to KSP.
    // The version should match your Kotlin version (2.0.0).
    // id("kotlin-kapt") version "2.0.0" apply false // Uncomment and use if necessary
}
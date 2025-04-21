// app/build.gradle.kts

// Required import for Properties class
import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.dagger.hilt.android")
    id("com.google.devtools.ksp")
    id("androidx.room")
    // id("kotlin-kapt")
}

// --- Load local.properties file (Moved outside android block) ---
val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties") // Reference from project root
if (localPropertiesFile.exists()) {
    // Use reader() for proper character encoding handling
    localPropertiesFile.reader(Charsets.UTF_8).use { reader ->
        localProperties.load(reader)
    }
}

// --- Configure Room Schema Location (Moved outside android block) ---
room {
    schemaDirectory("$projectDir/schemas")
}

android {
    namespace = "com.khan.scenes"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.khan.scenes"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        // --- Define buildConfigField for API Key ---
        // Get the value from the loaded properties
        val unsplashApiKey = localProperties.getProperty("UNSPLASH_API_KEY", "MISSING_API_KEY")

        // *** Use this line because local.properties HAS quotes around the key ***
        buildConfigField("String", "UNSPLASH_API_KEY", unsplashApiKey)

        // *** This line (with extra escaped quotes) should be commented out or removed ***
        // buildConfigField("String", "UNSPLASH_API_KEY", "\"$unsplashApiKey\"") // INCORRECT for your local.properties format
        // --- ---
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // Consider defining API key differently for release builds
        }
        debug {
            // Inherits buildConfigField from defaultConfig unless overridden
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17 // Update to 17
        targetCompatibility = JavaVersion.VERSION_17 // Update to 17
    }
    kotlinOptions {
        jvmTarget = "17" // Update to 17
    }
    buildFeatures {
        compose = true
        buildConfig = true // Ensure BuildConfig generation is enabled
    }

    composeOptions {
        // Leave empty when using org.jetbrains.kotlin.plugin.compose with Kotlin 2.0+
    }

    packagingOptions {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    // --- Ensure room block is NOT inside android block ---
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.0")
    implementation("androidx.activity:activity-compose:1.9.0")
    implementation(platform("androidx.compose:compose-bom:2024.05.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3") // Compose Material 3
    implementation("com.google.android.material:material:1.12.0") // Material Components (for XML themes)
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.0")
    implementation("androidx.navigation:navigation-compose:2.8.0-beta01")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.compose.material:material-icons-extended:1.6.8")

    // Hilt
    implementation("com.google.dagger:hilt-android:2.56.1")
    ksp("com.google.dagger:hilt-compiler:2.56.1")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

    // Ktor
    implementation("io.ktor:ktor-client-core:3.0.0-beta-1")
    implementation("io.ktor:ktor-client-android:3.0.0-beta-1")
    implementation("io.ktor:ktor-client-content-negotiation:3.0.0-beta-1")
    implementation("io.ktor:ktor-serialization-kotlinx-json:3.0.0-beta-1")
    implementation("io.ktor:ktor-client-logging:3.0.0-beta-1")

    // Coil
    implementation("io.coil-kt:coil-compose:2.6.0")

    // Room
    implementation("androidx.room:room-runtime:2.7.0")
    implementation("androidx.room:room-ktx:2.7.0")
    ksp("androidx.room:room-compiler:2.7.0")

    // DataStore
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    // WorkManager
    implementation("androidx.work:work-runtime-ktx:2.9.0")

    // Material 3 Adaptive
    implementation("androidx.compose.material3.adaptive:adaptive:1.0.0-beta03")
    implementation("androidx.compose.material3.adaptive:adaptive-layout:1.0.0-beta03") // Dependency was cut short in previous file, corrected here
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")
    // Testing Dependencies
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.05.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    // Hilt Testing (Optional)
    // testImplementation("com.google.dagger:hilt-android-testing:2.56.1")
    // kspTest("com.google.dagger:hilt-compiler:2.56.1")
    // androidTestImplementation("com.google.dagger:hilt-android-testing:2.56.1")
    // kspAndroidTest("com.google.dagger:hilt-compiler:2.56.1")
}

// --- KSP argument for Room schema location (Uncommented) ---
//ksp {
//    arg("room.schemaLocation", "$projectDir/schemas")
//}

// Configure KAPT if used
// kapt {
//    correctErrorTypes = true
// }
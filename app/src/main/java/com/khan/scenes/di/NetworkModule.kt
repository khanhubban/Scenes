package com.khan.scenes.di // Adjust package name as needed

import android.content.Context // Needed for OkHttp engine if used
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.*
import io.ktor.client.engine.android.* // Or io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class) // Lives as long as the application
object NetworkModule {

    @Provides
    @Singleton // Ensures only one instance is created
    fun provideKtorClient(): HttpClient {
        return HttpClient(Android) { // Or use OkHttp: HttpClient(OkHttp)
            // Configure default request parameters
            // defaultRequest {
            //    url("https://api.example.com/") // Set your base API URL here
            //    header("Authorization", "Bearer YOUR_API_KEY") // Example header if needed
            // }

            // Configure JSON serialization
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true // For easier debugging
                    isLenient = true // Allows slightly malformed JSON
                    ignoreUnknownKeys = true // IMPORTANT: Prevents crashes if API adds new fields
                })
            }

            // Configure Logging (useful for debugging)
            install(Logging) {
                level = LogLevel.BODY // Log request/response bodies. Other options: INFO, HEADERS, ALL
                // logger = Logger.DEFAULT // Or provide a custom logger
            }

            // Optional: Configure request retries, timeouts, etc.
            // install(HttpRequestRetry) { ... }
            // install(HttpTimeout) { ... }

            // Expect success: Throw exception for non-2xx responses
            expectSuccess = true
        }
    }
}


package com.khan.scenes.data.remote


import android.util.Log // Import Log
import com.khan.scenes.BuildConfig
import com.khan.scenes.data.remote.dto.SearchResponseDto
import com.khan.scenes.data.remote.dto.WallpaperDto
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.request.*
import io.ktor.http.HttpStatusCode // Import HttpStatusCode
import io.ktor.utils.io.errors.IOException
import javax.inject.Inject

class KtorWallpaperRemoteDataSource @Inject constructor(
    private val httpClient: HttpClient
) : WallpaperRemoteDataSource {

    private companion object {
        const val BASE_URL = "https://api.unsplash.com"
        const val ENDPOINT_PHOTOS = "/photos"
        const val ENDPOINT_SEARCH_PHOTOS = "/search/photos"
        // Base endpoint for single photo is also /photos/{id}
    }

    override suspend fun getWallpapers(page: Int, perPage: Int): List<WallpaperDto> {
        // (Keep existing implementation - truncated for brevity)
        return try {
            httpClient.get("$BASE_URL$ENDPOINT_PHOTOS") {
                parameter("page", page)
                parameter("per_page", perPage)
                parameter("client_id", BuildConfig.UNSPLASH_API_KEY)
            }.body()
        } catch (e: Exception) { // Simplified catch for brevity
            Log.e("KtorRemote", "Error fetching wallpapers: ${e.localizedMessage}", e)
            emptyList()
        }
    }

    override suspend fun searchWallpapers(query: String, page: Int, perPage: Int): SearchResponseDto {
        // (Keep existing implementation - truncated for brevity)
        val errorResponse = SearchResponseDto(total = 0, totalPages = 0, results = emptyList())
        return try {
            httpClient.get("$BASE_URL$ENDPOINT_SEARCH_PHOTOS") {
                parameter("query", query)
                parameter("page", page)
                parameter("per_page", perPage)
                parameter("client_id", BuildConfig.UNSPLASH_API_KEY)
            }.body<SearchResponseDto>()
        } catch (e: Exception) { // Simplified catch for brevity
            Log.e("KtorRemote", "Error searching wallpapers: ${e.localizedMessage}", e)
            errorResponse
        }
    }

    // *** ADD THIS IMPLEMENTATION ***
    override suspend fun getWallpaperDetails(id: String): WallpaperDto? {
        Log.d("KtorRemote", "Fetching details for ID: $id")
        return try {
            httpClient.get("$BASE_URL$ENDPOINT_PHOTOS/$id") { // Construct URL with ID
                parameter("client_id", BuildConfig.UNSPLASH_API_KEY)
            }.body() // Let Ktor parse the single WallpaperDto
        } catch (e: ClientRequestException) {
            // Handle 4xx errors specifically (like 404 Not Found)
            Log.e("KtorRemote", "Client Error fetching details for ID $id: ${e.response.status} ${e.localizedMessage}")
            if (e.response.status == HttpStatusCode.NotFound) {
                null // Return null if wallpaper ID doesn't exist on server
            } else {
                null // Return null for other client errors for now
            }
        } catch (e: ServerResponseException) {
            // Handle 5xx errors
            Log.e("KtorRemote", "Server Error fetching details for ID $id: ${e.response.status} ${e.localizedMessage}")
            null // Return null on server error
        } catch (e: IOException) {
            // Handle network connectivity issues
            Log.e("KtorRemote", "Network Error fetching details for ID $id: ${e.localizedMessage}")
            null // Return null on network error
        } catch (e: Exception) {
            // Handle other unexpected errors (e.g., parsing issues)
            Log.e("KtorRemote", "Generic Error fetching details for ID $id: ${e.localizedMessage}", e)
            null // Return null on generic error
        }
    }
    // ***************************
}
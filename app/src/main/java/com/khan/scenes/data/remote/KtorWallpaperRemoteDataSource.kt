package com.khan.scenes.data.remote


import android.util.Log // Ensure Log is imported
import com.khan.scenes.BuildConfig
import com.khan.scenes.data.remote.dto.SearchResponseDto
import com.khan.scenes.data.remote.dto.WallpaperDto
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.request.*
import io.ktor.http.HttpStatusCode
import io.ktor.utils.io.errors.IOException
import javax.inject.Inject

// Define a log tag for this class
private const val TAG = "KtorRemoteDataSource"

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
        Log.d(TAG, "Attempting to fetch wallpapers - page: $page, perPage: $perPage") // <-- ADDED LOG
        return try {
            val response: List<WallpaperDto> = httpClient.get("$BASE_URL$ENDPOINT_PHOTOS") {
                parameter("page", page)
                parameter("per_page", perPage)
                parameter("client_id", BuildConfig.UNSPLASH_API_KEY)
            }.body()
            Log.d(TAG, "Successfully fetched ${response.size} wallpapers for page $page") // <-- ADDED LOG
            response
        } catch (e: ClientRequestException) { // Specific error handling
            Log.e(TAG, "Client Error fetching wallpapers: ${e.response.status} - ${e.message}", e) // <-- MODIFIED LOG
            emptyList()
        } catch (e: ServerResponseException) {
            Log.e(TAG, "Server Error fetching wallpapers: ${e.response.status} - ${e.message}", e) // <-- MODIFIED LOG
            emptyList()
        } catch (e: IOException) {
            Log.e(TAG, "Network Error fetching wallpapers: ${e.message}", e) // <-- MODIFIED LOG
            emptyList()
        } catch (e: Exception) { // Generic catch
            Log.e(TAG, "Generic Error fetching wallpapers: ${e.message}", e) // <-- MODIFIED LOG
            emptyList()
        }
    }

    override suspend fun searchWallpapers(query: String, page: Int, perPage: Int): SearchResponseDto {
        Log.d(TAG, "Attempting to search wallpapers - query: '$query', page: $page, perPage: $perPage") // <-- ADDED LOG
        val errorResponse = SearchResponseDto(total = 0, totalPages = 0, results = emptyList())
        return try {
            val response: SearchResponseDto = httpClient.get("$BASE_URL$ENDPOINT_SEARCH_PHOTOS") {
                parameter("query", query)
                parameter("page", page)
                parameter("per_page", perPage)
                parameter("client_id", BuildConfig.UNSPLASH_API_KEY)
            }.body<SearchResponseDto>()
            Log.d(TAG, "Successfully fetched ${response.results.size} wallpapers for query '$query' page $page (Total: ${response.total})") // <-- ADDED LOG
            response
        } catch (e: ClientRequestException) {
            Log.e(TAG, "Client Error searching wallpapers ($query): ${e.response.status} - ${e.message}", e) // <-- MODIFIED LOG
            errorResponse
        } catch (e: ServerResponseException) {
            Log.e(TAG, "Server Error searching wallpapers ($query): ${e.response.status} - ${e.message}", e) // <-- MODIFIED LOG
            errorResponse
        } catch (e: IOException) {
            Log.e(TAG, "Network Error searching wallpapers ($query): ${e.message}", e) // <-- MODIFIED LOG
            errorResponse
        } catch (e: Exception) { // Generic catch
            Log.e(TAG, "Generic Error searching wallpapers ($query): ${e.message}", e) // <-- MODIFIED LOG
            errorResponse
        }
    }

    override suspend fun getWallpaperDetails(id: String): WallpaperDto? {
        Log.d(TAG, "Attempting to fetch details for ID: $id") // <-- MODIFIED LOG (was already present)
        return try {
            val response: WallpaperDto = httpClient.get("$BASE_URL$ENDPOINT_PHOTOS/$id") {
                parameter("client_id", BuildConfig.UNSPLASH_API_KEY)
            }.body()
            Log.d(TAG, "Successfully fetched details for ID: $id") // <-- ADDED LOG
            response
        } catch (e: ClientRequestException) {
            Log.e(TAG, "Client Error fetching details for ID $id: ${e.response.status} - ${e.message}", e) // <-- MODIFIED LOG
            null // Return null on client errors (like 404)
        } catch (e: ServerResponseException) {
            Log.e(TAG, "Server Error fetching details for ID $id: ${e.response.status} - ${e.message}", e) // <-- MODIFIED LOG
            null // Return null on server error
        } catch (e: IOException) {
            Log.e(TAG, "Network Error fetching details for ID $id: ${e.message}", e) // <-- MODIFIED LOG
            null // Return null on network error
        } catch (e: Exception) {
            Log.e(TAG, "Generic Error fetching details for ID $id: ${e.message}", e) // <-- MODIFIED LOG
            null // Return null on generic error
        }
    }
}
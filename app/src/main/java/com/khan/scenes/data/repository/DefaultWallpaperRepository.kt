package com.khan.scenes.data.repository // Adjust package if needed

import android.util.Log // Ensure Log is imported
import com.khan.scenes.data.local.db.dao.FavoritesDao
import com.khan.scenes.data.local.db.entity.FavoriteWallpaperEntity
import com.khan.scenes.data.remote.WallpaperRemoteDataSource
import com.khan.scenes.data.remote.dto.WallpaperDto
import com.khan.scenes.domain.model.Wallpaper
import com.khan.scenes.domain.repository.WallpaperRepository
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton

// Define a log tag for this class
private const val TAG = "WallpaperRepository"

@Singleton
class DefaultWallpaperRepository @Inject constructor(
    private val remoteDataSource: WallpaperRemoteDataSource,
    private val favoritesDao: FavoritesDao
) : WallpaperRepository {

    override fun getWallpapersFlow(page: Int, perPage: Int): Flow<List<Wallpaper>> = flow {
        Log.d(TAG, "getWallpapersFlow: Fetching page $page from remote source") // <-- ADDED LOG
        val dtoList = remoteDataSource.getWallpapers(page, perPage)
        Log.d(TAG, "getWallpapersFlow: Received ${dtoList.size} DTOs from remote for page $page") // <-- ADDED LOG
        // TODO: Combine with favorite status efficiently
        val domainList = dtoList.map { dto ->
            val isFav = isFavorite(dto.id) // Inefficient placeholder
            mapDtoToDomain(dto, isFavorite = isFav)
        }
        Log.d(TAG, "getWallpapersFlow: Emitting ${domainList.size} domain models for page $page") // <-- ADDED LOG
        emit(domainList)
    }.catch { e ->
        Log.e(TAG, "Error in getWallpapersFlow for page $page: ${e.localizedMessage}", e) // <-- MODIFIED LOG
        // Decide on error handling: re-throw or emit specific error state?
        // For now, let's re-throw to see it in the ViewModel
        throw e // <-- MODIFIED: Re-throwing instead of emitting emptyList()
        // emit(emptyList()) // Original behavior
    }

    override fun searchWallpapersFlow(query: String, page: Int, perPage: Int): Flow<List<Wallpaper>> = flow {
        Log.d(TAG, "searchWallpapersFlow: Searching '$query' page $page from remote source") // <-- ADDED LOG
        val searchResponse = remoteDataSource.searchWallpapers(query, page, perPage)
        Log.d(TAG, "searchWallpapersFlow: Received ${searchResponse.results.size} DTOs from remote for query '$query' page $page") // <-- ADDED LOG
        // TODO: Combine with favorite status efficiently
        val domainList = searchResponse.results.map { dto ->
            val isFav = isFavorite(dto.id) // Inefficient placeholder
            mapDtoToDomain(dto, isFavorite = isFav)
        }
        Log.d(TAG, "searchWallpapersFlow: Emitting ${domainList.size} domain models for query '$query' page $page") // <-- ADDED LOG
        emit(domainList)
    }.catch { e ->
        Log.e(TAG, "Error in searchWallpapersFlow for query '$query' page $page: ${e.localizedMessage}", e) // <-- MODIFIED LOG
        // Decide on error handling: re-throw or emit specific error state?
        // For now, let's re-throw to see it in the ViewModel
        throw e // <-- MODIFIED: Re-throwing instead of emitting emptyList()
        // emit(emptyList()) // Original behavior
    }

    override fun getWallpaperDetails(id: String): Flow<Wallpaper?> {
        Log.d(TAG, "getWallpaperDetails: Getting details for $id") // <-- MODIFIED LOG (was already present)

        val isFavoriteFlow: Flow<Boolean> = favoritesDao.getFavoriteById(id).map { it != null }
            .catch { e ->
                Log.e(TAG, "getWallpaperDetails: Error fetching favorite status for $id from DB: ${e.localizedMessage}", e) // <-- ADDED LOG
                emit(false)
            }

        val detailsFlow: Flow<WallpaperDto?> = flow {
            Log.d(TAG, "getWallpaperDetails: Fetching details for $id from remote source") // <-- ADDED LOG
            emit(remoteDataSource.getWallpaperDetails(id))
        }.catch { e ->
            // Catch error during the remote fetch itself if remote source doesn't handle it fully
            Log.e(TAG, "getWallpaperDetails: Error in detailsFlow for $id: ${e.localizedMessage}", e) // <-- ADDED LOG
            emit(null) // Emit null if the remote fetch flow itself fails
        }

        return detailsFlow.combine(isFavoriteFlow) { dto, isFav ->
            Log.d(TAG, "getWallpaperDetails: Combining details (DTO present: ${dto != null}) and favorite status ($isFav) for $id") // <-- ADDED LOG
            dto?.let { mapDtoToDomain(it, isFav) }
        }.catch { e ->
            Log.e(TAG, "Error combining details/fav status for $id: ${e.localizedMessage}", e) // <-- MODIFIED LOG
            emit(null)
        }
    }

    // --- Other functions remain the same ---
    override suspend fun toggleFavorite(wallpaperId: String): Boolean {
        val favorite = favoritesDao.getFavoriteByIdNow(wallpaperId)
        return if (favorite != null) {
            favoritesDao.deleteById(wallpaperId)
            Log.d(TAG, "Removed favorite (via toggle): $wallpaperId")
            false
        } else {
            Log.w(TAG, "Cannot add favorite (via toggle) with only ID: $wallpaperId. Add operation should use FavoritesRepository.")
            // Attempting to fetch full details to add - Requires remote call
            // This logic might be better placed elsewhere or require passing full Wallpaper object
            try {
                val details = remoteDataSource.getWallpaperDetails(wallpaperId)
                if (details != null) {
                    // Assuming addFavorite requires the domain model, but we don't have fav status yet
                    // This highlights complexity - maybe toggle should only *remove* here?
                    // Or FavoritesRepository should handle fetching if needed?
                    // For simplicity, let's stick to the original logic for now: toggle only removes.
                    Log.w(TAG, "Fetched details for $wallpaperId but add logic via toggle is complex/discouraged here.")
                    false // Keeping original behavior: toggle only removes if exists
                } else {
                    Log.w(TAG, "Could not fetch details for $wallpaperId to potentially add favorite.")
                    false
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching details during toggleFavorite add attempt for $wallpaperId", e)
                false
            }
        }
    }

    private suspend fun isFavorite(id: String): Boolean {
        val favoriteEntity = favoritesDao.getFavoriteByIdNow(id)
        return favoriteEntity != null
    }

    private fun mapDtoToDomain(dto: WallpaperDto, isFavorite: Boolean): Wallpaper {
        return Wallpaper(
            id = dto.id,
            smallUrl = dto.urls.small,
            regularUrl = dto.urls.regular,
            fullUrl = dto.urls.full,
            userName = dto.user?.name,
            userLink = dto.user?.links?.html,
            isFavorite = isFavorite
        )
    }

    private fun mapEntityToDomain(entity: FavoriteWallpaperEntity, isFavorite: Boolean = true): Wallpaper { // Assume true if entity exists
        return Wallpaper(
            id = entity.id,
            smallUrl = entity.smallUrl,
            regularUrl = entity.regularUrl,
            fullUrl = entity.fullUrl,
            userName = entity.userName,
            userLink = entity.userLink,
            isFavorite = isFavorite // Use parameter, default to true
        )
    }
}
package com.khan.scenes.data.repository // Adjust package if needed

import android.util.Log
import com.khan.scenes.data.local.db.dao.FavoritesDao
import com.khan.scenes.data.local.db.entity.FavoriteWallpaperEntity // Import Favorite entity
import com.khan.scenes.data.remote.WallpaperRemoteDataSource
import com.khan.scenes.data.remote.dto.WallpaperDto
import com.khan.scenes.domain.model.Wallpaper
import com.khan.scenes.domain.repository.WallpaperRepository
import kotlinx.coroutines.flow.* // Ensure flow operators like catch and combine are imported
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class DefaultWallpaperRepository @Inject constructor(
    private val remoteDataSource: WallpaperRemoteDataSource,
    private val favoritesDao: FavoritesDao
) : WallpaperRepository {

    // getWallpapersFlow and searchWallpapersFlow remain the same (still have TODO for efficiency)
    override fun getWallpapersFlow(page: Int, perPage: Int): Flow<List<Wallpaper>> = flow {
        Log.d("Repo", "Fetching page $page")
        val dtoList = remoteDataSource.getWallpapers(page, perPage)
        // TODO: Combine with favorite status efficiently
        val domainList = dtoList.map { dto ->
            val isFav = isFavorite(dto.id) // Inefficient placeholder
            mapDtoToDomain(dto, isFavorite = isFav)
        }
        emit(domainList)
    }.catch { e ->
        Log.e("Repo", "Error in getWallpapersFlow: ${e.localizedMessage}", e)
        emit(emptyList())
    }

    override fun searchWallpapersFlow(query: String, page: Int, perPage: Int): Flow<List<Wallpaper>> = flow {
        Log.d("Repo", "Searching '$query' page $page")
        val searchResponse = remoteDataSource.searchWallpapers(query, page, perPage)
        // TODO: Combine with favorite status efficiently
        val domainList = searchResponse.results.map { dto ->
            val isFav = isFavorite(dto.id) // Inefficient placeholder
            mapDtoToDomain(dto, isFavorite = isFav)
        }
        emit(domainList)
    }.catch { e ->
        Log.e("Repo", "Error in searchWallpapersFlow: ${e.localizedMessage}", e)
        emit(emptyList())
    }
    // *********************************************************************

    // *** UPDATED getWallpaperDetails IMPLEMENTATION ***
    override fun getWallpaperDetails(id: String): Flow<Wallpaper?> {
        Log.d("Repo", "Getting details for $id from remote and combining with favorite status.")

        // Flow to observe favorite status from the DAO
        val isFavoriteFlow: Flow<Boolean> = favoritesDao.getFavoriteById(id).map { it != null }
            .catch { emit(false) } // Emit false if DB query fails

        // Flow to fetch details from the remote source
        val detailsFlow: Flow<WallpaperDto?> = flow {
            emit(remoteDataSource.getWallpaperDetails(id))
        } // Network errors handled within remoteDataSource, returns null on error

        // Combine the remote details Flow and the favorite status Flow
        return detailsFlow.combine(isFavoriteFlow) { dto, isFav ->
            // If DTO is not null (network fetch succeeded), map it to domain model
            // Otherwise (network error or 404), the result is null
            dto?.let { mapDtoToDomain(it, isFav) }
        }.catch { e ->
            // Catch any unexpected errors during combine or mapping
            Log.e("Repo", "Error combining details/fav status for $id: ${e.localizedMessage}", e)
            emit(null) // Emit null on error
        }
    }
    // *************************************************

    override suspend fun toggleFavorite(wallpaperId: String): Boolean {
        // (Keep implementation from previous step)
        val favorite = favoritesDao.getFavoriteByIdNow(wallpaperId)
        return if (favorite != null) {
            favoritesDao.deleteById(wallpaperId)
            Log.d("DefaultWallpaperRepo", "Removed favorite (via toggle): $wallpaperId")
            false
        } else {
            Log.w("DefaultWallpaperRepo", "Cannot add favorite (via toggle) with only ID: $wallpaperId. Add operation should use FavoritesRepository.")
            false
        }
    }

    private suspend fun isFavorite(id: String): Boolean {
        // (Keep implementation from previous step)
        val favoriteEntity = favoritesDao.getFavoriteByIdNow(id)
        return favoriteEntity != null
    }

    private fun mapDtoToDomain(dto: WallpaperDto, isFavorite: Boolean): Wallpaper {
        // (Keep implementation from previous step)
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

    private fun mapEntityToDomain(entity: FavoriteWallpaperEntity, isFavorite: Boolean): Wallpaper {
        // (Keep implementation from previous step)
        return Wallpaper(
            id = entity.id,
            smallUrl = entity.smallUrl,
            regularUrl = entity.regularUrl,
            fullUrl = entity.fullUrl,
            userName = entity.userName,
            userLink = entity.userLink,
            isFavorite = isFavorite
        )
    }
}
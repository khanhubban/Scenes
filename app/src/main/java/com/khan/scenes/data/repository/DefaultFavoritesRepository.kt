package com.khan.scenes.data.repository // Adjust package if needed

import android.util.Log // Import Log if you add logging
import com.khan.scenes.data.local.db.dao.FavoritesDao
import com.khan.scenes.data.local.db.entity.FavoriteWallpaperEntity
import com.khan.scenes.domain.model.Wallpaper
import com.khan.scenes.domain.repository.FavoritesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultFavoritesRepository @Inject constructor(
    private val favoritesDao: FavoritesDao
) : FavoritesRepository {

    override fun getFavoritesFlow(): Flow<List<Wallpaper>> {
        // Get flow of entities from DAO and map each entity to the domain model
        return favoritesDao.getAllFavorites().map { entityList ->
            entityList.map { mapEntityToDomain(it) }
        }
    }

    // --- Completed addFavorite function ---
    override suspend fun addFavorite(wallpaper: Wallpaper) {
        Log.d("FavoritesRepository", "Adding favorite: ${wallpaper.id}") // Optional logging
        // Map domain model to entity
        val favoriteEntity = mapDomainToEntity(wallpaper)
        // Insert using DAO
        favoritesDao.insert(favoriteEntity)
    }
    // --- End completed function ---

    override suspend fun removeFavorite(wallpaperId: String) {
        Log.d("FavoritesRepository", "Removing favorite: $wallpaperId") // Optional logging
        favoritesDao.deleteById(wallpaperId)
    }

    override fun isFavoriteFlow(wallpaperId: String): Flow<Boolean> {
        // Observe the specific favorite entry and map its existence to a boolean
        return favoritesDao.getFavoriteById(wallpaperId).map { it != null }
    }

    // --- Mapping Functions (Ensure these are present) ---
    private fun mapEntityToDomain(entity: FavoriteWallpaperEntity): Wallpaper {
        return Wallpaper(
            id = entity.id,
            smallUrl = entity.smallUrl,
            regularUrl = entity.regularUrl,
            fullUrl = entity.fullUrl,
            // Use smallUrl as fallback thumbnail for favorites loaded from DB
            thumbUrl = entity.smallUrl,
            userName = entity.userName,
            userLink = entity.userLink,
            isFavorite = true, // Assumed true if it exists as an entity
            // Other fields are null as they aren't stored in FavoriteWallpaperEntity
            description = null,
            width = null,
            height = null,
            downloads = null,
            tags = null
        )
    }

    private fun mapDomainToEntity(wallpaper: Wallpaper): FavoriteWallpaperEntity {
        return FavoriteWallpaperEntity(
            id = wallpaper.id,
            smallUrl = wallpaper.smallUrl,
            regularUrl = wallpaper.regularUrl,
            fullUrl = wallpaper.fullUrl,
            // Note: We don't store thumbUrl, description, width, height etc. in the entity currently.
            // Only the fields defined in FavoriteWallpaperEntity are saved.
            userName = wallpaper.userName,
            userLink = wallpaper.userLink
            // addedTimestamp is handled by default value in Entity definition
        )
    }
}
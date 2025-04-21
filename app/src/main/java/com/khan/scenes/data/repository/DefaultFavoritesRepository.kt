package com.khan.scenes.data.repository // Adjust package if needed

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

    override suspend fun addFavorite(wallpaper: Wallpaper) {
        // Map domain model to entity and insert using DAO
        favoritesDao.insert(mapDomainToEntity(wallpaper))
    }

    override suspend fun removeFavorite(wallpaperId: String) {
        favoritesDao.deleteById(wallpaperId)
    }

    override fun isFavoriteFlow(wallpaperId: String): Flow<Boolean> {
        // Observe the specific favorite entry and map its existence to a boolean
        return favoritesDao.getFavoriteById(wallpaperId).map { it != null }
    }

    // --- Mapping Functions ---
    private fun mapEntityToDomain(entity: FavoriteWallpaperEntity): Wallpaper {
        return Wallpaper(
            id = entity.id,
            smallUrl = entity.smallUrl,
            regularUrl = entity.regularUrl,
            fullUrl = entity.fullUrl,
            userName = entity.userName,
            userLink = entity.userLink,
            isFavorite = true // Assumed true if it exists as an entity
        )
    }

    private fun mapDomainToEntity(wallpaper: Wallpaper): FavoriteWallpaperEntity {
        return FavoriteWallpaperEntity(
            id = wallpaper.id,
            smallUrl = wallpaper.smallUrl,
            regularUrl = wallpaper.regularUrl,
            fullUrl = wallpaper.fullUrl,
            userName = wallpaper.userName,
            userLink = wallpaper.userLink
            // addedTimestamp is handled by default value in Entity
        )
    }
}
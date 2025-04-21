package com.khan.scenes.data.repository // Adjust package if needed

// --- Necessary Imports ---
import android.util.Log
import com.khan.scenes.data.local.db.dao.FavoritesDao // Needed for isFavorite check
import com.khan.scenes.data.local.db.entity.FavoriteWallpaperEntity // Keep ONLY IF mapEntityToDomain is kept
import com.khan.scenes.data.remote.WallpaperRemoteDataSource
import com.khan.scenes.data.remote.dto.WallpaperDto
import com.khan.scenes.domain.model.Wallpaper
import com.khan.scenes.domain.repository.WallpaperRepository
import kotlinx.coroutines.flow.* // Includes Flow, flow, catch, map, combine, first, etc.
import javax.inject.Inject
import javax.inject.Singleton
// --- End Imports ---

// Define a log tag for this class
private const val TAG = "WallpaperRepository"

@Singleton
class DefaultWallpaperRepository @Inject constructor(
    private val remoteDataSource: WallpaperRemoteDataSource,
    private val favoritesDao: FavoritesDao // Keep FavoritesDao for isFavorite lookups
) : WallpaperRepository {

    override fun getWallpapersFlow(page: Int, perPage: Int): Flow<List<Wallpaper>> = flow {
        Log.d(TAG, "getWallpapersFlow: Fetching page $page from remote source")
        // Fetch favorite IDs once for efficient checking
        val favoriteIds: Set<String> = try {
            favoritesDao.getAllFavorites().first().map { it.id }.toSet()
        } catch (e: Exception) {
            Log.e(TAG, "getWallpapersFlow: Error fetching favorite IDs: ${e.message}", e)
            emptySet()
        }
        Log.d(TAG, "getWallpapersFlow: Fetched ${favoriteIds.size} favorite IDs.")

        // Assuming remoteDataSource throws errors now if needed
        val dtoList = remoteDataSource.getWallpapers(page, perPage)
        Log.d(TAG, "getWallpapersFlow: Received ${dtoList.size} DTOs from remote for page $page")

        // Map DTOs to Domain models, checking against the favorite IDs set
        val domainList = dtoList.map { dto ->
            mapDtoToDomain(dto, isFavorite = dto.id in favoriteIds) // Efficient check
        }
        Log.d(TAG, "getWallpapersFlow: Emitting ${domainList.size} domain models for page $page")
        emit(domainList)
    }.catch { e ->
        Log.e(TAG, "Error in getWallpapersFlow for page $page: ${e.localizedMessage}", e)
        throw e // Re-throw to be handled by ViewModel
    }

    override fun searchWallpapersFlow(query: String, page: Int, perPage: Int): Flow<List<Wallpaper>> = flow<List<Wallpaper>> {
        Log.d(TAG, "searchWallpapersFlow: Searching '$query' page $page from remote source")
        // Fetch favorite IDs once for efficient checking
        val favoriteIds: Set<String> = try {
            favoritesDao.getAllFavorites().first().map { it.id }.toSet()
        } catch (e: Exception) {
            Log.e(TAG, "searchWallpapersFlow: Error fetching favorite IDs: ${e.message}", e)
            emptySet()
        }
        Log.d(TAG, "searchWallpapersFlow: Fetched ${favoriteIds.size} favorite IDs.")

        // Assuming remoteDataSource throws errors now if needed
        val searchResponse = remoteDataSource.searchWallpapers(query, page, perPage)
        Log.d(TAG, "searchWallpapersFlow: Received ${searchResponse.results.size} DTOs from remote for query '$query' page $page")

        // Map DTOs to Domain models, checking against the favorite IDs set
        val domainList = searchResponse.results.map { dto ->
            mapDtoToDomain(dto, isFavorite = dto.id in favoriteIds) // Efficient check
        }
        Log.d(TAG, "searchWallpapersFlow: Emitting ${domainList.size} domain models for query '$query' page $page")
        emit(domainList)
    }.catch { e ->
        Log.e(TAG, "Error in searchWallpapersFlow for query '$query' page $page: ${e.localizedMessage}", e)
        throw e // Re-throw to be handled by ViewModel
    }

    override fun getWallpaperDetails(id: String): Flow<Wallpaper?> {
        Log.d(TAG, "getWallpaperDetails: Getting details for $id")

        // Flow to check if the ID exists in favorites DB
        val isFavoriteFlow: Flow<Boolean> = favoritesDao.getFavoriteById(id).map { it != null }
            .catch { e ->
                Log.e(TAG, "getWallpaperDetails: Error fetching favorite status for $id from DB: ${e.localizedMessage}", e)
                emit(false) // Default to false if DB flow fails
            }

        // Flow to fetch details from remote source
        val detailsFlow: Flow<WallpaperDto?> = flow {
            Log.d(TAG, "getWallpaperDetails: Fetching details for $id from remote source")
            // Assuming remoteDataSource.getWallpaperDetails throws on error now
            emit(remoteDataSource.getWallpaperDetails(id))
        }.catch { e ->
            // Catch errors specifically from the details network call
            Log.e(TAG, "getWallpaperDetails: Error in remote detailsFlow for $id: ${e.localizedMessage}", e)
            // Re-throw or emit null based on desired handling
            // Emitting null if details can't be fetched
            emit(null)
            // Alternatively: throw e
        }

        // Combine the remote details flow and the favorite status flow
        return detailsFlow.combine(isFavoriteFlow) { dto, isFav ->
            Log.d(TAG, "getWallpaperDetails: Combining details (DTO present: ${dto != null}) and favorite status ($isFav) for $id")
            // If DTO is null (e.g., 404 or network error), result is null
            // Otherwise, map the DTO with the determined favorite status
            dto?.let { mapDtoToDomain(it, isFav) }
        }.catch { e ->
            // Catch errors during the combine operation or from upstream if re-thrown
            Log.e(TAG, "Error combining details/fav status for $id: ${e.localizedMessage}", e)
            emit(null) // Emit null if combine fails
        }
    }


    // --- Mapping Function (DTO -> Domain) ---
    // Kept and updated to include thumbUrl
    private fun mapDtoToDomain(dto: WallpaperDto, isFavorite: Boolean): Wallpaper {
        val effectiveDescription = dto.description ?: dto.altDescription
        val tagTitles = dto.tags?.mapNotNull { it.title }

        return Wallpaper(
            id = dto.id,
            smallUrl = dto.urls.small,
            regularUrl = dto.urls.regular,
            fullUrl = dto.urls.full,
            thumbUrl = dto.urls.thumb, // Map thumbUrl
            userName = dto.user?.name,
            userLink = dto.user?.links?.html,
            isFavorite = isFavorite,
            description = effectiveDescription,
            width = dto.width,
            height = dto.height,
            downloads = dto.downloads,
            tags = tagTitles
        )
    }

    // --- REMOVED mapEntityToDomain function ---
    // This function is not needed in this repository, as mapping favorites
    // from the database is handled by DefaultFavoritesRepository.
    // private fun mapEntityToDomain(entity: FavoriteWallpaperEntity, isFavorite: Boolean = true): Wallpaper { ... }

}
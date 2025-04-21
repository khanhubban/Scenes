package com.khan.scenes.ui.browse // Correct package

// Necessary Imports for the AsyncImage version
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage // Use basic AsyncImage
import coil.request.ImageRequest
import com.khan.scenes.R
import com.khan.scenes.domain.model.Wallpaper
// Removed unused imports like SubcomposeAsyncImage, Shimmer, CoroutineScope etc.

@Composable
fun WallpaperGridItem( // Ensure only this composable is defined in this file
    wallpaper: Wallpaper,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Logging URL attempt
    Log.d("WallpaperGridItem", "Attempting to load URL: ${wallpaper.smallUrl} for ID: ${wallpaper.id} (Using basic AsyncImage)")

    Card(
        modifier = modifier
            .aspectRatio(0.75f)
            .clickable(onClick = onClick)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Use basic AsyncImage (which worked)
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(wallpaper.smallUrl) // Use smallUrl
                    .crossfade(true)
                    // Define error drawable in builder
                    .error(R.drawable.ic_error)
                    .build(),
                // Define placeholder painter directly on AsyncImage
                placeholder = ColorPainter(MaterialTheme.colorScheme.surfaceVariant),
                // Define error painter directly on AsyncImage
                error = painterResource(R.drawable.ic_error),
                contentDescription = stringResource(
                    R.string.content_description_wallpaper_preview,
                    wallpaper.userName ?: "User"
                ),
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
                onError = { errorResult ->
                    Log.e(
                        "WallpaperGridItem",
                        "Error loading image with AsyncImage: ${wallpaper.smallUrl}",
                        errorResult.result.throwable
                    )
                },
                onSuccess = { successResult ->
                    Log.d("WallpaperGridItem", "Successfully loaded image with AsyncImage: ${wallpaper.smallUrl}")
                }
            )

            // Favorite indicator logic
            if (wallpaper.isFavorite) {
                Icon(
                    imageVector = Icons.Filled.Favorite,
                    contentDescription = stringResource(R.string.content_description_favorite),
                    tint = Color.Red,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(24.dp)
                )
            }
        } // End Box
    } // End Card
} // End WallpaperGridItem composable
package com.khan.scenes.ui.browse // Adjust package if needed

// --- AndroidX & Compose Imports ---
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.* // imports Column, Row, Box, etc.
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.icons.Icons // Import default icons
import androidx.compose.material.icons.filled.Favorite // Import Favorite icon
import androidx.compose.material.icons.outlined.FavoriteBorder // Import Favorite outline icon
import androidx.compose.material3.*
import androidx.compose.runtime.* // imports remember, LaunchedEffect, etc.
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.* // Import flow operators

// --- Coil Imports ---
import coil.compose.AsyncImage // Make sure this import is present
import coil.request.ImageRequest // Make sure this import is present

// --- Project Imports ---
import com.khan.scenes.R // Import R class
import com.khan.scenes.domain.model.Wallpaper // Import domain model
// Removed duplicate BrowseScreenState definition reference here, already imported implicitly
// import com.khan.scenes.ui.browse.BrowseScreenState
// import com.khan.scenes.ui.browse.BrowseIntent


@OptIn(ExperimentalMaterial3Api::class) // For Scaffold and TopAppBar
@Composable
fun BrowseScreen( // Renamed back from WallpaperBrowseScreen
    onWallpaperClick: (wallpaperId: String) -> Unit,
    viewModel: BrowseViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Trigger initial load only once
    LaunchedEffect(key1 = Unit) {
        viewModel.processIntent(BrowseIntent.LoadInitialWallpapers)
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(title = { Text("Browse Wallpapers") })
            // TODO: Add SearchBar later (Section 7.3)
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues) // Apply padding from Scaffold
        ) {
            when (val state = uiState) {
                is BrowseScreenState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is BrowseScreenState.Success -> {
                    // Check if initial load (query==null) resulted in empty list
                    if (state.wallpapers.isEmpty() && state.query == null) {
                        Text(
                            text = "No wallpapers found.",
                            modifier = Modifier.align(Alignment.Center),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    } else {
                        WallpaperGrid(
                            wallpapers = state.wallpapers,
                            canLoadMore = state.canLoadMore, // Pass flag
                            onWallpaperClick = onWallpaperClick,
                            onFavoriteToggle = { wallpaperId ->
                                // Send ToggleFavorite intent
                                viewModel.processIntent(BrowseIntent.ToggleFavorite(wallpaperId))
                            },
                            onLoadMore = {
                                // Send LoadNextPage intent
                                viewModel.processIntent(BrowseIntent.LoadNextPage)
                            }
                        )
                    }
                }
                is BrowseScreenState.Error -> {
                    // Show error message and a retry button
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Error: ${state.message}",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = {
                            // Send intent to retry initial load
                            viewModel.processIntent(BrowseIntent.LoadInitialWallpapers)
                        }) {
                            Text("Retry")
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun WallpaperGrid(
    wallpapers: List<Wallpaper>,
    canLoadMore: Boolean, // Receive the flag
    onLoadMore: () -> Unit, // Callback to trigger loading more
    onWallpaperClick: (wallpaperId: String) -> Unit,
    onFavoriteToggle: (wallpaperId: String) -> Unit, // Callback for favorite toggle
    modifier: Modifier = Modifier
) {
    val gridState = rememberLazyGridState() // Remember the grid state

    LazyVerticalGrid(
        state = gridState, // Pass the state to the grid
        columns = GridCells.Adaptive(minSize = 150.dp), // Responsive grid columns
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(8.dp), // Padding around the grid
        horizontalArrangement = Arrangement.spacedBy(8.dp), // Spacing between columns
        verticalArrangement = Arrangement.spacedBy(8.dp) // Spacing between rows
    ) {
        items(items = wallpapers, key = { wallpaper -> wallpaper.id }) { wallpaper ->
            // WallpaperGridItem composable defined below
            WallpaperGridItem(
                wallpaper = wallpaper,
                onClick = { onWallpaperClick(wallpaper.id) },
                onFavoriteToggle = { onFavoriteToggle(wallpaper.id) } // Pass toggle callback
            )
        }

        // Add item for pagination loading indicator at the end if needed
        if (canLoadMore) {
            item { // Span across all columns if needed, or just appear as a single item
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }

    // Side effect to detect when user scrolls near the end
    LaunchedEffect(gridState, canLoadMore) {
        snapshotFlow { gridState.layoutInfo.visibleItemsInfo }
            .map { visibleItems ->
                // Check if the last visible item index is close to the total item count
                visibleItems.lastOrNull()?.index ?: -1
            }
            .distinctUntilChanged()
            .filter { lastVisibleIndex ->
                val totalItems = gridState.layoutInfo.totalItemsCount
                // Trigger load more when near the end (e.g., last 5 items visible)
                // and when not currently loading and can actually load more
                canLoadMore && totalItems > 0 && lastVisibleIndex >= totalItems - 5 // Check against canLoadMore
            }
            .collect {
                Log.d("BrowseScreen", "Load more triggered")
                onLoadMore() // Call the load more callback
            }
    }
}


// *** Updated WallpaperGridItem Implementation ***
@Composable
fun WallpaperGridItem(
    wallpaper: Wallpaper,
    onClick: () -> Unit,
    onFavoriteToggle: () -> Unit, // Receive favorite toggle callback
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .aspectRatio(1f) // Make item square-ish (adjust ratio as needed e.g., 0.7f for portrait)
            .clickable(onClick = onClick), // Handle clicks on the card itself
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Use Coil to load the image
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(wallpaper.smallUrl) // Load small URL for grid efficiency
                    .crossfade(true) // Enable crossfade animation
                    .placeholder(R.drawable.ic_placeholder_image) // Your placeholder
                    .error(R.drawable.ic_error) // Your error placeholder
                    .build(),
                contentDescription = "Wallpaper by ${wallpaper.userName ?: "Unknown"}",
                contentScale = ContentScale.Crop, // Crop image to fill the bounds
                modifier = Modifier.fillMaxSize() // Image fills the Box/Card
            )

            // Favorite Icon Button in the corner
            IconToggleButton(
                checked = wallpaper.isFavorite,
                onCheckedChange = { onFavoriteToggle() }, // Call lambda on toggle
                modifier = Modifier
                    .align(Alignment.TopEnd) // Position at the top-right corner
                    .padding(4.dp) // Add some padding around the button
                // Optional: Add a background scrim for better icon visibility
                // .background(Color.Black.copy(alpha = 0.3f), shape = CircleShape)
            ) {
                Icon(
                    imageVector = if (wallpaper.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = "Toggle Favorite",
                    // Tint the icon: Red when favorite, White when not (adjust as needed)
                    tint = if (wallpaper.isFavorite) Color.Red else Color.White
                )
            }
        }
    }
}
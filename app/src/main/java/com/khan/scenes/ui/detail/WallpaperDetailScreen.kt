package com.khan.scenes.ui.detail // Adjust package if needed

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack // Use auto-mirrored icon
import androidx.compose.material.icons.filled.Favorite // Import favorite icons
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.khan.scenes.R // Import R class
import com.khan.scenes.domain.model.Wallpaper // Import domain model


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WallpaperDetailScreen(
    onNavigateUp: () -> Unit,
    viewModel: WallpaperDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Extract wallpaper data when in Success state for easier access
    val currentWallpaper = (uiState as? DetailScreenState.Success)?.wallpaper

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    // Show user name in title if available
                    Text(
                        text = currentWallpaper?.userName ?: "Details",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                // *** Add Favorite Action Icon ***
                actions = {
                    // Show favorite toggle only when details are loaded successfully
                    if (currentWallpaper != null) {
                        IconToggleButton(
                            checked = currentWallpaper.isFavorite,
                            onCheckedChange = {
                                viewModel.processIntent(DetailIntent.ToggleFavorite)
                            }
                        ) {
                            Icon(
                                imageVector = if (currentWallpaper.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                contentDescription = "Toggle Favorite",
                                tint = if (currentWallpaper.isFavorite) Color.Red else LocalContentColor.current // Use default tint or white/red
                            )
                        }
                    }
                    // Add other actions like download, share later
                },
                colors = TopAppBarDefaults.topAppBarColors( // Make AppBar slightly transparent
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
            // Apply padding only to content *inside* the box if needed,
            // Image should go edge-to-edge behind status/nav bars
            // .padding(paddingValues) // Typically don't apply padding here if image is background
            ,
            contentAlignment = Alignment.Center
        ) {
            when (val state = uiState) {
                is DetailScreenState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.padding(paddingValues)) // Apply padding here
                }
                is DetailScreenState.Success -> {
                    // Use a Box to layer image and details overlay
                    Box(modifier = Modifier.fillMaxSize()) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(state.wallpaper.regularUrl) // Or fullUrl for max quality
                                .crossfade(true)
                                .placeholder(R.drawable.ic_placeholder_image)
                                .error(R.drawable.ic_error)
                                .build(),
                            contentDescription = "Wallpaper by ${state.wallpaper.userName ?: "Unknown"}",
                            contentScale = ContentScale.Crop, // Crop might look better edge-to-edge
                            modifier = Modifier.fillMaxSize() // Image fills the screen edge-to-edge
                        )

                        // *** Add Details Overlay at the bottom ***
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.BottomCenter)
                                .background(
                                    // Gradient scrim from transparent to slightly opaque black
                                    Brush.verticalGradient(
                                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.6f)),
                                        startY = 0f,
                                        endY = Float.POSITIVE_INFINITY // Adjust endY if needed
                                    )
                                )
                                .padding(bottom = 16.dp, start = 16.dp, end = 16.dp, top = 32.dp) // Add padding + extra bottom padding
                        ) {
                            Column {
                                Text(
                                    text = "Photo by ${state.wallpaper.userName ?: "Unknown"}",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                // Add photographer link later if needed
                                // state.wallpaper.userLink?.let { link ->
                                //    ClickableText(...)
                                // }
                            }
                        }
                        // ****************************************
                    }
                }
                is DetailScreenState.Error -> {
                    Text(
                        text = "Error: ${state.message}",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(paddingValues) // Apply padding here
                    )
                }
            }
        }
    }
}
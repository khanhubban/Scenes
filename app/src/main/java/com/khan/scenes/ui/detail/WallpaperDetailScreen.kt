package com.khan.scenes.ui.detail // Adjust package if needed

import android.widget.Toast // *** Import Toast ***
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Wallpaper // Icon itself
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect // *** Import LaunchedEffect ***
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
import com.khan.scenes.R
// No direct import needed for domain model Wallpaper if using currentWallpaper variable
// import com.khan.scenes.domain.model.Wallpaper
import kotlinx.coroutines.flow.collectLatest // *** Import collectLatest ***


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WallpaperDetailScreen(
    onNavigateUp: () -> Unit,
    viewModel: WallpaperDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val currentWallpaper = (uiState as? DetailScreenState.Success)?.wallpaper
    val context = LocalContext.current // Get context for Toasts

    // *** Add LaunchedEffect to observe events ***
    LaunchedEffect(Unit) {
        viewModel.uiEvents.collectLatest { event ->
            when(event) {
                is DetailScreenEvent.ShowToast -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
                // Handle other events later if needed
            }
        }
    }
    // ******************************************

    Scaffold(
        // (Keep existing Scaffold setup)
        topBar = {
            TopAppBar(
                title = {
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
                actions = {
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
                                tint = if (currentWallpaper.isFavorite) Color.Red else LocalContentColor.current
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black.copy(alpha = 0.3f),
                    navigationIconContentColor = Color.White,
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            // (Keep existing when(state) block for Loading/Success/Error)
            when (val state = uiState) {
                is DetailScreenState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center){
                        CircularProgressIndicator()
                    }
                }
                is DetailScreenState.Success -> {
                    Box(modifier = Modifier.fillMaxSize()) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(state.wallpaper.regularUrl) // Use regular or full
                                .crossfade(true)
                                .placeholder(R.drawable.ic_placeholder_image)
                                .error(R.drawable.ic_error)
                                .build(),
                            contentDescription = "Wallpaper by ${state.wallpaper.userName ?: "Unknown"}",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )

                        // Bottom Overlay
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.BottomCenter)
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f))
                                    )
                                )
                                .padding(WindowInsets.navigationBars.asPaddingValues()) // Handle nav bar insets
                                .padding(horizontal = 16.dp, vertical = 16.dp)

                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text( // Photographer Name
                                    text = "Photo by ${currentWallpaper?.userName ?: "Unknown"}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f, fill = false).padding(end = 8.dp)
                                )
                                Row { // Action Buttons
                                    IconButton(onClick = { viewModel.processIntent(DetailIntent.DownloadWallpaper) }) {
                                        Icon(Icons.Filled.Download, "Download", tint = Color.White)
                                    }
                                    Spacer(Modifier.width(8.dp))
                                    IconButton(onClick = { viewModel.processIntent(DetailIntent.SetWallpaper) }) {
                                        Icon(Icons.Filled.Wallpaper, "Set as Wallpaper", tint = Color.White)
                                    }
                                }
                            }
                        }
                    }
                }
                is DetailScreenState.Error -> {
                    Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center){
                        Text(
                            text = "Error: ${state.message}",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            } // end when(state)
        } // end Box
    } // end Scaffold
}
package com.khan.scenes.ui.browse

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.khan.scenes.R
import com.khan.scenes.domain.model.Wallpaper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrowseScreen(
    viewModel: BrowseViewModel = hiltViewModel(),
    onWallpaperClick: (String) -> Unit,
    onSettingsClick: () -> Unit
) {
    // Fix 1: Change browseState to uiState to match the property name in BrowseViewModel
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.app_name)) },
                actions = {
                    IconButton(onClick = onSettingsClick) {
                        Icon(
                            imageVector = Icons.Filled.Settings,
                            contentDescription = stringResource(R.string.content_description_settings)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->

        // Fix 2: Change browseState references to uiState
        when (uiState) {
            is BrowseScreenState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is BrowseScreenState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = (uiState as BrowseScreenState.Error).message ?: stringResource(R.string.unknown_error),
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
            is BrowseScreenState.Success -> {
                val wallpapers = (uiState as BrowseScreenState.Success).wallpapers
                if (wallpapers.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(stringResource(R.string.no_wallpapers_found))
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 128.dp),
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentPadding = PaddingValues(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(wallpapers, key = { it.id }) { wallpaper ->
                            WallpaperGridItem(
                                wallpaper = wallpaper,
                                onClick = { onWallpaperClick(wallpaper.id) }
                            )
                        }
                        // You might want to add pagination loading indicator here
                    }
                }
            }
        }
    }
}

@Composable
fun WallpaperGridItem(
    wallpaper: Wallpaper,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .aspectRatio(1f)
            .clickable(onClick = onClick)
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(wallpaper.smallUrl)
                .crossfade(true)
                .build(),
            contentDescription = stringResource(R.string.content_description_wallpaper_preview, wallpaper.userName ?: "User"),
            placeholder = painterResource(R.drawable.ic_placeholder_image),
            error = painterResource(R.drawable.ic_error),
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        // Optional: Add favorite indicator overlay if needed
    }
}
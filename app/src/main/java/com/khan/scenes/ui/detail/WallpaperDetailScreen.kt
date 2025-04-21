package com.khan.scenes.ui.detail

import android.util.Log
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer // <-- Correct import added
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.imageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.khan.scenes.R
import com.valentinilk.shimmer.shimmer // Import Shimmer for loading effect
import kotlinx.coroutines.CoroutineScope // Keep scope imports
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WallpaperDetailScreen(
    viewModel: WallpaperDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope() // Keep scope if needed for other actions

    // State Management
    var showOverlay by remember { mutableStateOf(true) }
    var fullResRequested by remember { mutableStateOf(false) }
    var isLoadingFullRes by remember { mutableStateOf(false) } // For full-res shimmer
    var regularImageAlpha by remember { mutableStateOf(0f) } // For regular image fade-in
    // --- End State ---


    LaunchedEffect(Unit) {
        viewModel.uiEvents.collectLatest { event ->
            when(event) {
                is DetailScreenEvent.ShowToast -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when (val state = uiState) {
            is DetailScreenState.Loading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
            is DetailScreenState.Error -> {
                Text(
                    text = stringResource(R.string.error_loading_details, state.message),
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center).padding(16.dp)
                )
            }
            is DetailScreenState.Success -> {
                val wallpaper = state.wallpaper

                // Reset alpha when wallpaper changes
                LaunchedEffect(wallpaper.id) {
                    regularImageAlpha = 0f
                    fullResRequested = false
                }

                // Determine final URL for the main image loader (Regular or Full)
                val finalImageUrl = if (!showOverlay && fullResRequested) {
                    wallpaper.fullUrl
                } else {
                    wallpaper.regularUrl // Target regular initially (or when overlay visible)
                }

                // Box to hold Thumbnail, Main Image, and potential loading indicator/shimmer overlay
                Box(modifier = Modifier.fillMaxSize()) {

                    // 1. Thumbnail Image (LQIP) - Always attempt to load first
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(wallpaper.thumbUrl)
                            .crossfade(false) // No fade needed for placeholder
                            .build(),
                        contentDescription = null, // Decorative
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    // 2. Main Image (Regular or Full) - Fades in over thumbnail
                    val animatedRegularImageAlpha by animateFloatAsState(targetValue = regularImageAlpha, label = "RegularImageAlpha")
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(finalImageUrl)
                            .crossfade(false) // We handle fade with alpha
                            .error(R.drawable.ic_error)
                            .listener(
                                onStart = { request ->
                                    Log.d("DetailScreen", "Main AsyncImage Start loading: ${request.data}")
                                    // Reset alpha when starting load for Regular/Full
                                    regularImageAlpha = 0f
                                    // Keep shimmer visible only if loading Full URL
                                    isLoadingFullRes = (request.data == wallpaper.fullUrl && !showOverlay)
                                },
                                onSuccess = { request, result ->
                                    val loadedUrl = request.data
                                    Log.d("DetailScreen", "Main AsyncImage Successfully loaded: $loadedUrl")
                                    // Fade in the image
                                    regularImageAlpha = 1f
                                    // Hide full-res indicator unconditionally on success
                                    isLoadingFullRes = false
                                },
                                onError = { request, result ->
                                    Log.e("DetailScreen", "Main AsyncImage Error loading: ${request.data}", result.throwable)
                                    // Hide full-res indicator on error
                                    isLoadingFullRes = false
                                    // Keep alpha at 0, showing only the thumb on error
                                    regularImageAlpha = 0f
                                }
                            )
                            .build(),
                        contentDescription = stringResource(
                            if (showOverlay) R.string.content_description_wallpaper_detail else R.string.content_description_wallpaper_detail_immersive,
                            wallpaper.userName ?: stringResource(R.string.unknown_photographer)
                        ),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable { // Click toggles overlay and requests Full res load
                                showOverlay = !showOverlay
                                // Request full res only the first time overlay is hidden
                                if (!showOverlay && !fullResRequested) {
                                    Log.d("DetailScreen", "Requesting full resolution.")
                                    fullResRequested = true // Trigger loading 'fullUrl'
                                    isLoadingFullRes = true // Show shimmer
                                    Toast.makeText(context, "Loading full resolution...", Toast.LENGTH_SHORT).show()
                                } else {
                                    // If overlay is shown OR full res already requested, ensure shimmer is off
                                    isLoadingFullRes = false
                                }
                            }
                            .graphicsLayer { alpha = animatedRegularImageAlpha } // Apply alpha animation
                    )

                    // 3. Loading Shimmer Overlay (Only for Full Res request)
                    AnimatedVisibility(
                        visible = isLoadingFullRes,
                        modifier = Modifier.fillMaxSize(),
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .shimmer()
                                .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.5f))
                        )
                    }

                } // End Box holding images and indicator/shimmer

                // 4. Content Overlay Column (AnimatedVisibility)
                AnimatedVisibility(
                    visible = showOverlay,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth(),
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f),
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            // Header Row ... (content as before)
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                                Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer)){Icon(Icons.Default.Person, contentDescription = "User Avatar", modifier = Modifier.align(Alignment.Center))}
                                Spacer(Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = wallpaper.description ?: stringResource(R.string.wallpaper_title_fallback), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Text(text = wallpaper.userName ?: stringResource(R.string.unknown_photographer), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }
                                Spacer(Modifier.width(12.dp))
                                IconToggleButton(checked = wallpaper.isFavorite, onCheckedChange = { viewModel.processIntent(DetailIntent.ToggleFavorite) }) { Icon(imageVector = if (wallpaper.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder, contentDescription = stringResource(R.string.content_description_toggle_favorite), tint = if (wallpaper.isFavorite) Color.Red else MaterialTheme.colorScheme.onSurfaceVariant) }
                            }
                            Spacer(Modifier.height(16.dp))
                            // Action Buttons Row ... (content as before)
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                Button(onClick = { viewModel.processIntent(DetailIntent.DownloadWallpaper) }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(percent = 50)) { Icon(Icons.Filled.Download, contentDescription = null, modifier = Modifier.size(ButtonDefaults.IconSize)); Spacer(Modifier.size(ButtonDefaults.IconSpacing)); Text(stringResource(R.string.action_download)) }
                                Button(onClick = { viewModel.processIntent(DetailIntent.SetWallpaper) }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(percent = 50)) { Icon(Icons.Filled.Wallpaper, contentDescription = null, modifier = Modifier.size(ButtonDefaults.IconSize)); Spacer(Modifier.size(ButtonDefaults.IconSpacing)); Text(stringResource(R.string.action_set_wallpaper)) }
                            }
                            // Description Text ... (content as before)
                            wallpaper.description?.takeIf { it.isNotBlank() }?.let { desc -> Spacer(Modifier.height(16.dp)); Text(desc, style = MaterialTheme.typography.bodyMedium,) }
                            Spacer(Modifier.height(16.dp))
                            Divider()
                            Spacer(Modifier.height(16.dp))
                            // Info Grid/List ... (content as before)
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { val downloadsText = wallpaper.downloads?.let { NumberFormat.getNumberInstance(Locale.US).format(it) } ?: "N/A"; InfoItem(icon = Icons.Filled.Download, text = stringResource(R.string.info_downloads, downloadsText)); val resolutionText = if (wallpaper.width != null && wallpaper.height != null) { "${wallpaper.width} x ${wallpaper.height}" } else { stringResource(R.string.info_not_available) }; InfoItem(icon = Icons.Filled.AspectRatio, text = resolutionText) }
                            Spacer(Modifier.height(8.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { val tagText = wallpaper.tags?.firstOrNull() ?: stringResource(R.string.info_not_available); InfoItem(icon = Icons.Filled.Style, text = tagText); InfoItem(icon = Icons.Filled.SaveAlt, text = stringResource(R.string.info_size_na)) }
                            Spacer(Modifier.height(8.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { InfoItem(icon = Icons.Filled.Info, text = stringResource(R.string.info_license_placeholder)); InfoItem(icon = Icons.Filled.Report, text = stringResource(R.string.info_report)) }
                        } // End Column inside Surface
                    } // End Surface
                } // End AnimatedVisibility for Overlay
            } // End When state
        } // End Root Box holding Background/Overlay
    } // End Outer Box
}


// Helper Composable for the Info Items (Icon + Text) - Unchanged
@Composable
private fun InfoItem(icon: ImageVector, text: String, modifier: Modifier = Modifier) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
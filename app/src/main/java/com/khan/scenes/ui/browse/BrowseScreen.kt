package com.khan.scenes.ui.browse

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.* // Import remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.khan.scenes.R
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map


@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun BrowseScreen(
    viewModel: BrowseViewModel = hiltViewModel(),
    onWallpaperClick: (String) -> Unit,
    onSettingsClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val categories = viewModel.categories

    val snackbarHostState = remember { SnackbarHostState() }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val listState = rememberLazyGridState()

    val currentQuery = (uiState as? BrowseScreenState.Success)?.query ?: ""
    val selectedCategory = (uiState as? BrowseScreenState.Success)?.selectedCategory

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
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

        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            OutlinedTextField(
                value = currentQuery,
                onValueChange = { viewModel.processIntent(BrowseIntent.SearchQueryChanged(it)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                label = { Text(stringResource(R.string.search_wallpapers_label)) },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                trailingIcon = {
                    if (currentQuery.isNotEmpty()) {
                        IconButton(onClick = {
                            viewModel.processIntent(BrowseIntent.SearchQueryChanged(""))
                            viewModel.processIntent(BrowseIntent.TriggerSearch)
                            keyboardController?.hide()
                            focusManager.clearFocus()
                        }) {
                            Icon(Icons.Filled.Clear, contentDescription = stringResource(R.string.content_description_clear_search))
                        }
                    }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        viewModel.processIntent(BrowseIntent.TriggerSearch)
                        keyboardController?.hide()
                        focusManager.clearFocus()
                    }
                )
            )

            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { category ->
                    // Remember the onClick lambda for FilterChip
                    val rememberedChipOnClick = remember(category, selectedCategory) {
                        {
                            val newCategory = if (category == selectedCategory) null else category
                            viewModel.processIntent(BrowseIntent.CategorySelected(newCategory))
                            keyboardController?.hide()
                            focusManager.clearFocus()
                        }
                    }
                    FilterChip(
                        selected = category == selectedCategory,
                        onClick = rememberedChipOnClick, // Use remembered lambda
                        label = { Text(category) },
                    )
                }
            }

            when (val state = uiState) {
                is BrowseScreenState.Loading -> {
                    Log.d("BrowseScreen", "Rendering Loading state")
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                is BrowseScreenState.Error -> {
                    Log.d("BrowseScreen", "Rendering Error state: ${state.message}")
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = state.message,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
                is BrowseScreenState.Success -> {
                    Log.d("BrowseScreen", "Rendering Success state. Wallpaper count: ${state.wallpapers.size}")
                    if (state.wallpapers.isEmpty()) {
                        Log.d("BrowseScreen", "Rendering Empty Success state")
                        Box(
                            modifier = Modifier.fillMaxSize().padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(stringResource(R.string.no_wallpapers_found))
                        }
                    } else {
                        Log.d("BrowseScreen", "Rendering Success state with grid for ${state.wallpapers.size} items")
                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(minSize = 128.dp),
                            state = listState,
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(state.wallpapers, key = { it.id }) { wallpaper ->
                                Log.d("BrowseScreen", "Composing item for ID: ${wallpaper.id}")

                                // Remember the onClick lambda for WallpaperGridItem
                                val rememberedItemOnClick = remember(wallpaper.id, onWallpaperClick) {
                                    { onWallpaperClick(wallpaper.id) }
                                }

                                WallpaperGridItem(
                                    wallpaper = wallpaper,
                                    onClick = rememberedItemOnClick // Use remembered lambda
                                )
                            }

                            if (state.isLoadingMore) {
                                item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
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

                        LaunchedEffect(listState, state.canLoadMore, state.isLoadingMore) {
                            snapshotFlow { listState.layoutInfo.visibleItemsInfo }
                                .map { visibleItems ->
                                    val lastVisibleItemIndex = visibleItems.lastOrNull()?.index ?: -1
                                    val totalItems = listState.layoutInfo.totalItemsCount
                                    lastVisibleItemIndex >= totalItems - 6 && state.canLoadMore && !state.isLoadingMore && totalItems > 0
                                }
                                .distinctUntilChanged()
                                .filter { shouldLoadMore -> shouldLoadMore }
                                .collect {
                                    Log.d("BrowseScreen", "Pagination triggered - loading next page")
                                    viewModel.processIntent(BrowseIntent.LoadNextPage)
                                }
                        }
                    }
                }
            }
        }
    }
}
package com.khan.scenes.ui.settings

import android.util.Log // Keep Log if using placeholder Log.d
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite // Import icon if using
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle // Import if needed
import com.khan.scenes.R // Import R
import com.khan.scenes.ui.navigation.AppDestinations // Import if needed for Log

// Data class SettingsUiState remains the same...



// ViewModel remains the same...
// @HiltViewModel class SettingsViewModel @Inject constructor(...) { ... }


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onNavigateUp: () -> Unit,
    // *** Add this new parameter ***
    onNavigateToFavorites: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) { // Use existing onNavigateUp
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.content_description_back)
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
                .padding(vertical = 8.dp)
        ) {
            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                SettingsSectionTitle(title = stringResource(R.string.settings_section_appearance))
                DarkThemeSettingItem(
                    isChecked = uiState.isDarkThemeEnabled,
                    onCheckedChange = { viewModel.updateDarkTheme(it) }
                )

                Divider(modifier = Modifier.padding(vertical = 8.dp))

                SettingsSectionTitle(title = stringResource(R.string.settings_section_auto_change))
                SettingsInfoItem(
                    title = stringResource(R.string.settings_item_interval),
                    value = uiState.autoChangeInterval,
                    onClick = { /* TODO: Implement selection logic */ }
                )
                SettingsInfoItem(
                    title = stringResource(R.string.settings_item_source),
                    value = uiState.autoChangeSource,
                    onClick = { /* TODO: Implement selection logic */ }
                )

                Divider(modifier = Modifier.padding(vertical = 8.dp))

                // --- Navigation to Favorites ---
                ListItem(
                    modifier = Modifier.clickable {
                        // *** Use the new callback parameter ***
                        onNavigateToFavorites()
                    },
                    headlineContent = { Text(stringResource(R.string.settings_item_view_favorites)) },
                    // Optional: Add leading icon
                    leadingContent = { Icon(Icons.Default.Favorite, contentDescription = null) }
                )
                // --- End Navigation to Favorites ---
            }
        }
    }
}

// Helper composables (SettingsSectionTitle, DarkThemeSettingItem, SettingsInfoItem) remain the same...

// Example SettingsSectionTitle (if not already present from previous context)
@Composable
fun SettingsSectionTitle(title: String, modifier: Modifier = Modifier) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier
            .padding(horizontal = 16.dp)
            .padding(top = 16.dp, bottom = 8.dp)
    )
}

// Example DarkThemeSettingItem (if not already present from previous context)
@Composable
fun DarkThemeSettingItem(
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    ListItem(
        modifier = modifier
            .clickable(onClick = { onCheckedChange(!isChecked) })
            .padding(horizontal = 16.dp),
        headlineContent = { Text(stringResource(R.string.settings_item_dark_theme)) },
        trailingContent = {
            Switch(
                checked = isChecked,
                onCheckedChange = onCheckedChange
            )
        }
    )
}

// Example SettingsInfoItem (if not already present from previous context)
@Composable
fun SettingsInfoItem(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val clickableModifier = if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier
    ListItem(
        modifier = modifier
            .then(clickableModifier)
            .padding(horizontal = 16.dp),
        headlineContent = { Text(title) },
        supportingContent = { Text(value) }
    )
}


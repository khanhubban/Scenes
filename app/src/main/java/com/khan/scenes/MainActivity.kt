package com.khan.scenes // Your base package

// Keep necessary imports from previous versions
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text // Keep for Preview
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel // Keep hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle // Keep collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.khan.scenes.ui.browse.BrowseScreen // Import Browse Screen
import com.khan.scenes.ui.detail.WallpaperDetailScreen // Import Detail Screen
import com.khan.scenes.ui.favorites.FavoritesScreen // Import Favorites Screen
import com.khan.scenes.ui.navigation.AppDestinations // Import Destinations
import com.khan.scenes.ui.settings.SettingsScreen // Import Settings Screen
import com.khan.scenes.ui.settings.SettingsViewModel // Import Settings VM
import com.khan.scenes.ui.theme.ScenesTheme // Import Theme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            // Observe dark theme state from SettingsViewModel for theme selection
            val settingsViewModel: SettingsViewModel = hiltViewModel()
            // Ensure SettingsViewModel is correctly providing state
            val settingsState by settingsViewModel.uiState.collectAsStateWithLifecycle()

            // Pass the dark theme preference to ScenesTheme
            ScenesTheme(darkTheme = settingsState.isDarkThemeEnabled) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation() // Call the NavHost composable
                }
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController: NavHostController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = AppDestinations.BROWSE_ROUTE
    ) {
        // Browse Screen Destination
        composable(route = AppDestinations.BROWSE_ROUTE) {
            BrowseScreen(
                onWallpaperClick = { wallpaperId ->
                    navController.navigate("${AppDestinations.DETAIL_ROUTE}/$wallpaperId")
                },
                onSettingsClick = {
                    navController.navigate(AppDestinations.SETTINGS_ROUTE)
                }
            )
        }

        // Detail Screen Destination
        composable(
            route = AppDestinations.detailRouteWithArg,
            arguments = listOf(navArgument(AppDestinations.WALLPAPER_ID_ARG) { type = NavType.StringType })
        ) { backStackEntry ->
            WallpaperDetailScreen(
                // onNavigateUp parameter removed from definition and call
                // viewModel = hiltViewModel() // Implicitly handled by Hilt
            )
        }

        // Settings Screen Destination
        composable(route = AppDestinations.SETTINGS_ROUTE) {
            SettingsScreen(
                onNavigateUp = { navController.navigateUp() },
                // *** Pass the lambda for the new parameter ***
                onNavigateToFavorites = { navController.navigate(AppDestinations.FAVORITES_ROUTE) }
            )
        }

        // Favorites Screen Destination
        composable(route = AppDestinations.FAVORITES_ROUTE) {
            FavoritesScreen(
                onWallpaperClick = { wallpaperId ->
                    navController.navigate("${AppDestinations.DETAIL_ROUTE}/$wallpaperId")
                },
                onNavigateUp = { navController.navigateUp() }
            )
        }
    }
}


@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    // Use a fixed theme state for preview if needed
    ScenesTheme(darkTheme = false) {
        Surface(modifier = Modifier.fillMaxSize()) {
            // Previewing AppNavigation might require more setup (dummy NavController, ViewModels)
            // Consider previewing individual screens directly if AppNavigation preview is complex
            Text("Preview - Displaying a specific screen might be easier.")
            // Example: Preview settings screen
            // SettingsScreen(onNavigateUp = {}, onNavigateToFavorites = {}) // Preview needs dummy lambdas
        }
    }
}
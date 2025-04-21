package com.khan.scenes // Your base package

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text // Keep Text import for error case or placeholders
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController // Import NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.khan.scenes.ui.browse.BrowseScreen
import com.khan.scenes.ui.detail.WallpaperDetailScreen
import com.khan.scenes.ui.navigation.AppDestinations // Make sure this is imported
import com.khan.scenes.ui.settings.SettingsScreen    // <-- Import the new SettingsScreen
import com.khan.scenes.ui.theme.ScenesTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ScenesTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Call the composable containing the NavHost
                    AppNavigation()
                }
            }
        }
    }
}

// Composable function containing the navigation graph
@Composable
fun AppNavigation() {
    val navController: NavHostController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = AppDestinations.BROWSE_ROUTE // Your initial screen
    ) {
        // Browse Screen Destination
        composable(route = AppDestinations.BROWSE_ROUTE) {
            BrowseScreen(
                onWallpaperClick = { wallpaperId ->
                    // Navigate to detail, ensuring ID is passed correctly
                    navController.navigate("${AppDestinations.DETAIL_ROUTE}/$wallpaperId")
                },
                // *** Add navigation to Settings here (or wherever you place the trigger) ***
                onSettingsClick = { // Example: Add this callback to BrowseScreen parameters
                    navController.navigate(AppDestinations.SETTINGS_ROUTE)
                }
                // Add other necessary parameters/callbacks to BrowseScreen
            )
        }

        // Detail Screen Destination
        composable(
            route = AppDestinations.detailRouteWithArg,
            arguments = listOf(navArgument(AppDestinations.WALLPAPER_ID_ARG) { type = NavType.StringType })
        ) { backStackEntry ->
            WallpaperDetailScreen(
                onNavigateUp = { navController.navigateUp() }
            )
        }

        // --- >>>> ADD THIS COMPOSABLE ENTRY <<<< ---
        composable(route = AppDestinations.SETTINGS_ROUTE) {
            SettingsScreen(
                onNavigateUp = { navController.navigateUp() } // Handles back navigation
            )
        }
        // --- >>>> END OF ADDED ENTRY <<<< ---

    }
}


@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    // It's good practice to preview the navigation composable or individual screens
    // Previewing AppNavigation might require providing a dummy NavController
    // For simplicity, you might preview BrowseScreen or SettingsScreen directly here
    ScenesTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            // Example: Preview the AppNavigation structure
            AppNavigation()
            // Or preview a specific screen:
            // SettingsScreen(onNavigateUp = {})
        }
    }
}
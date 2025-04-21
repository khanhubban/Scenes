package com.khan.scenes // Your base package

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text // Keep Text import for error case
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.khan.scenes.ui.browse.BrowseScreen
import com.khan.scenes.ui.navigation.AppDestinations
import com.khan.scenes.ui.theme.ScenesTheme
// *** Import the new Detail Screen Composable ***
import com.khan.scenes.ui.detail.WallpaperDetailScreen
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
                    AppNavigation()
                }
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = AppDestinations.BROWSE_ROUTE
    ) {
        // Browse Screen Destination
        composable(route = AppDestinations.BROWSE_ROUTE) {
            BrowseScreen(
                onWallpaperClick = { wallpaperId ->
                    navController.navigate("${AppDestinations.DETAIL_ROUTE}/$wallpaperId")
                }
            )
        }

        // Detail Screen Destination
        composable(
            route = AppDestinations.detailRouteWithArg,
            arguments = listOf(navArgument(AppDestinations.WALLPAPER_ID_ARG) { type = NavType.StringType })
        ) { backStackEntry ->
            // ViewModel retrieves the ID, so we don't strictly need to pass it here,
            // but it's useful if the Screen composable needs it directly for some reason.
            // val wallpaperId = backStackEntry.arguments?.getString(AppDestinations.WALLPAPER_ID_ARG)

            // *** Replace Placeholder with actual screen call ***
            WallpaperDetailScreen(
                onNavigateUp = { navController.navigateUp() } // Provide lambda for back navigation
            )
            // **************************************************

            // --- Original placeholder code for reference ---
            // val wallpaperId = backStackEntry.arguments?.getString(AppDestinations.WALLPAPER_ID_ARG)
            // if (wallpaperId != null) {
            //     Text("Detail Screen for ID: $wallpaperId") // Placeholder was here
            // } else {
            //     Text("Error: Wallpaper ID missing.")
            // }
            // --- End of original placeholder ---
        }
    }
}


@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    ScenesTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            AppNavigation()
        }
    }
}
package com.khan.scenes.ui.navigation // Or your preferred package

object AppDestinations {
    const val BROWSE_ROUTE = "browse"
    const val DETAIL_ROUTE = "detail"
    const val SETTINGS_ROUTE = "settings" // <-- Add this line
    const val WALLPAPER_ID_ARG = "wallpaperId" // Argument name

    // Route for detail screen, including the argument placeholder
    val detailRouteWithArg = "$DETAIL_ROUTE/{$WALLPAPER_ID_ARG}"
}
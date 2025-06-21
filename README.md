Scenes Wallpaper App
Scenes is a modern, offline-first wallpaper application for Android, built entirely with Jetpack Compose and the latest Android development technologies. It provides a clean and simple interface for users to browse, search, and set wallpapers on their devices.

Features
Dynamic UI: A responsive and modern UI built with Jetpack Compose.
Dynamic Theming: Supports both light and dark themes, which can be toggled in settings. The theme also adapts to the system's dark mode setting.
Browse & Search: Users can browse an endless grid of wallpapers and search for specific images.
Categories: Quick filtering of wallpapers by predefined categories like "Nature," "Abstract," and "Dark".
Wallpaper Details: A detailed view for each wallpaper, showing information like photographer, resolution, and downloads. It features an immersive mode to view the wallpaper without UI clutter.
Favorites: Users can save their favorite wallpapers, which are stored locally in a Room database for offline access.
Set & Download: Users can set wallpapers directly to their device's home screen or download them to their local storage.
Automatic Wallpaper Changer: The app can automatically change the device wallpaper periodically using WorkManager. Users can configure the interval (e.g., 1 hour, 6 hours, daily) and the source (e.g., Favorites, Random) from the settings screen.
Offline First: Favorite wallpapers are saved to a local Room database, making them available even without a network connection.
Architecture
The application follows the principles of Clean Architecture, separating concerns into three main layers:

UI Layer (scenes/ui): Built with Jetpack Compose. It consists of Composable functions for screens, ViewModels (BrowseViewModel, WallpaperDetailViewModel, etc.) that manage UI state, and MVI-style intents to handle user actions.
Domain Layer (scenes/domain): This layer contains the core business logic. It includes the application's models (e.g., Wallpaper) and repository interfaces (WallpaperRepository, FavoritesRepository, SettingsRepository) that define the contracts for data operations.
Data Layer (scenes/data): Responsible for providing data to the domain layer. It contains repository implementations (DefaultWallpaperRepository, DefaultFavoritesRepository) and data sources.
Remote: KtorWallpaperRemoteDataSource handles network requests to the backend API.
Local: AppDatabase (Room) for managing favorite wallpapers and PreferencesSettingsDataSource (DataStore) for user settings.
Technology Stack
Language: Kotlin
UI: Jetpack Compose for a fully declarative UI.
Asynchronous Programming: Kotlin Coroutines and Flow for managing background threads and handling streams of data.
Dependency Injection: Hilt for managing dependencies throughout the app.
Networking: Ktor Client for making HTTP requests to the wallpaper API.
Database: Room for persisting favorite wallpapers locally.
Data Persistence: Jetpack DataStore for storing user settings.
Background Processing: WorkManager for scheduling the automatic wallpaper changing task.
Image Loading: Coil for loading and caching images.
Navigation: Jetpack Navigation Compose for navigating between screens.
![photo-collage png (1)](https://github.com/user-attachments/assets/c6f8d1ec-c11d-4e07-a1d4-49dc1426621e)





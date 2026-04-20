package com.guitarapp.songsbook

import android.os.Bundle
import com.google.android.gms.ads.MobileAds
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.guitarapp.songsbook.data.local.ThemeMode
import com.guitarapp.songsbook.data.local.UserPreferences
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.automirrored.outlined.QueueMusic
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.guitarapp.songsbook.data.local.SongDatabase
import com.guitarapp.songsbook.data.repository.AssetSongRepository
import com.guitarapp.songsbook.data.repository.PlaylistRepository
import com.guitarapp.songsbook.data.repository.RoomPlaylistRepository
import com.guitarapp.songsbook.data.repository.SongRepository
import com.guitarapp.songsbook.presentation.Routes
import com.guitarapp.songsbook.presentation.screens.AboutScreen
import com.guitarapp.songsbook.presentation.screens.AddSongScreen
import com.guitarapp.songsbook.presentation.screens.PreviewReaderScreen
import com.guitarapp.songsbook.presentation.screens.FavoritesScreen
import com.guitarapp.songsbook.presentation.screens.HomeScreen
import com.guitarapp.songsbook.presentation.screens.PlaylistDetailScreen
import com.guitarapp.songsbook.presentation.screens.PlaylistsScreen
import com.guitarapp.songsbook.presentation.screens.SettingsScreen
import com.guitarapp.songsbook.presentation.screens.SongReaderScreen
import com.guitarapp.songsbook.presentation.viewmodel.AddSongViewModel
import com.guitarapp.songsbook.presentation.viewmodel.FavoritesViewModel
import com.guitarapp.songsbook.presentation.viewmodel.HomeViewModel
import com.guitarapp.songsbook.presentation.viewmodel.PlaylistsViewModel
import com.guitarapp.songsbook.presentation.viewmodel.ReaderViewModel
import com.guitarapp.songsbook.ui.theme.GuitarSongsbookTheme

data class BottomNavItem(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

private val bottomNavItems = listOf(
    BottomNavItem(Routes.HOME, "Home", Icons.Filled.Home, Icons.Outlined.Home),
    BottomNavItem(Routes.FAVORITES, "Favorites", Icons.Filled.Favorite, Icons.Outlined.FavoriteBorder),
    BottomNavItem(Routes.PLAYLISTS, "Playlists", Icons.AutoMirrored.Filled.QueueMusic, Icons.AutoMirrored.Outlined.QueueMusic)
)

class MainActivity : ComponentActivity() {

    var themeMode by mutableStateOf(ThemeMode.SYSTEM)

    private val database by lazy { SongDatabase.getInstance(this) }
    private val songRepository: SongRepository by lazy {
        AssetSongRepository(assets, database.songDao())
    }
    private val playlistRepository: PlaylistRepository by lazy {
        RoomPlaylistRepository(database.playlistDao())
    }

    private val homeViewModel: HomeViewModel by viewModels {
        HomeViewModel.Factory(songRepository)
    }

    private val favoritesViewModel: FavoritesViewModel by viewModels {
        FavoritesViewModel.Factory(songRepository)
    }

    private val playlistsViewModel: PlaylistsViewModel by viewModels {
        PlaylistsViewModel.Factory(playlistRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        MobileAds.initialize(this)
        enableEdgeToEdge()
        themeMode = UserPreferences.getThemeMode(this)
        setContent {
            val darkTheme = when (themeMode) {
                ThemeMode.DARK -> true
                ThemeMode.LIGHT -> false
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
            }
            GuitarSongsbookTheme(darkTheme = darkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentDestination = navBackStackEntry?.destination
                    val showBottomBar = currentDestination?.route in bottomNavItems.map { it.route }

                    Scaffold(
                        bottomBar = {
                            if (showBottomBar) {
                                GuitarBottomBar(
                                    currentDestination = currentDestination,
                                    onTabSelected = { route ->
                                        navController.navigate(route) {
                                            popUpTo(Routes.HOME) { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                )
                            }
                        }
                    ) { paddingValues ->
                        GuitarNavHost(
                            navController = navController,
                            songRepository = songRepository,
                            playlistRepository = playlistRepository,
                            homeViewModel = homeViewModel,
                            favoritesViewModel = favoritesViewModel,
                            playlistsViewModel = playlistsViewModel,
                            onThemeModeChanged = { mode ->
                                UserPreferences.setThemeMode(this@MainActivity, mode)
                                themeMode = mode
                            },
                            modifier = Modifier.padding(paddingValues)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GuitarBottomBar(
    currentDestination: NavDestination?,
    onTabSelected: (String) -> Unit
) {
    NavigationBar {
        bottomNavItems.forEach { item ->
            val selected = currentDestination?.hierarchy?.any {
                it.route == item.route
            } == true

            NavigationBarItem(
                selected = selected,
                onClick = { if (!selected) onTabSelected(item.route) },
                icon = {
                    Icon(
                        imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = item.label
                    )
                },
                label = { Text(item.label) }
            )
        }
    }
}

@Composable
private fun GuitarNavHost(
    navController: NavHostController,
    songRepository: SongRepository,
    playlistRepository: PlaylistRepository,
    homeViewModel: HomeViewModel,
    favoritesViewModel: FavoritesViewModel,
    playlistsViewModel: PlaylistsViewModel,
    onThemeModeChanged: (ThemeMode) -> Unit,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Routes.HOME,
        modifier = modifier,
        enterTransition = {
            slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(300))
        },
        exitTransition = {
            slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(300))
        },
        popEnterTransition = {
            slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(300))
        },
        popExitTransition = {
            slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(300))
        }
    ) {
        composable(Routes.HOME) {
            LaunchedEffect(Unit) {
                homeViewModel.refreshSongs()
            }
            HomeScreen(
                viewModel = homeViewModel,
                onSongClick = { songId -> navController.navigate(Routes.reader(songId)) },
                onEditClick = { songId -> navController.navigate(Routes.editSong(songId)) },
                onAddSongClick = { navController.navigate(Routes.ADD_SONG) },
                onSettingsClick = { navController.navigate(Routes.SETTINGS) }
            )
        }
        composable(Routes.FAVORITES) {
            LaunchedEffect(Unit) {
                favoritesViewModel.loadFavorites()
            }
            FavoritesScreen(
                viewModel = favoritesViewModel,
                onSongClick = { songId -> navController.navigate(Routes.reader(songId)) }
            )
        }
        composable(Routes.PLAYLISTS) {
            LaunchedEffect(Unit) {
                playlistsViewModel.loadPlaylists()
            }
            PlaylistsScreen(
                viewModel = playlistsViewModel,
                onPlaylistClick = { playlistId ->
                    navController.navigate(Routes.playlistDetail(playlistId))
                }
            )
        }
        composable(
            route = Routes.PLAYLIST_DETAIL,
            arguments = listOf(navArgument("playlistId") { type = NavType.LongType })
        ) { backStackEntry ->
            val playlistId = backStackEntry.arguments?.getLong("playlistId") ?: return@composable
            LaunchedEffect(playlistId) {
                playlistsViewModel.loadPlaylistDetail(playlistId)
            }
            PlaylistDetailScreen(
                viewModel = playlistsViewModel,
                onSongClick = { songId -> navController.navigate(Routes.reader(songId)) },
                onBackClick = { navController.popBackStack() }
            )
        }
        composable(Routes.ADD_SONG) {
            val addSongViewModel: AddSongViewModel = viewModel(
                factory = AddSongViewModel.Factory(songRepository)
            )
            AddSongScreen(
                viewModel = addSongViewModel,
                onBackClick = { navController.popBackStack() },
                onPreviewClick = {
                    val preview = addSongViewModel.buildPreviewSong()
                    if (preview != null) {
                        AddSongViewModel.pendingPreview = preview
                        navController.navigate(Routes.PREVIEW)
                    }
                },
                onSaveSuccess = {
                    navController.popBackStack()
                }
            )
        }
        composable(Routes.PREVIEW) {
            val previewSong = remember { AddSongViewModel.pendingPreview }
            if (previewSong != null) {
                PreviewReaderScreen(
                    song = previewSong,
                    onBackClick = {
                        AddSongViewModel.pendingPreview = null
                        navController.popBackStack()
                    }
                )
            } else {
                LaunchedEffect(Unit) {
                    navController.popBackStack()
                }
            }
        }
        composable(Routes.SETTINGS) {
            SettingsScreen(
                onBackClick = { navController.popBackStack() },
                onAboutClick = { navController.navigate(Routes.ABOUT) },
                onThemeModeChanged = onThemeModeChanged
            )
        }
        composable(Routes.ABOUT) {
            AboutScreen(onBackClick = { navController.popBackStack() })
        }
        composable(
            route = Routes.EDIT_SONG,
            arguments = listOf(navArgument("songId") { type = NavType.StringType })
        ) { backStackEntry ->
            val songId = backStackEntry.arguments?.getString("songId") ?: return@composable
            val editSongViewModel: AddSongViewModel = viewModel(
                factory = AddSongViewModel.Factory(songRepository, editSongId = songId)
            )
            AddSongScreen(
                viewModel = editSongViewModel,
                onBackClick = { navController.popBackStack() },
                onPreviewClick = {
                    val preview = editSongViewModel.buildPreviewSong()
                    if (preview != null) {
                        AddSongViewModel.pendingPreview = preview
                        navController.navigate(Routes.PREVIEW)
                    }
                },
                onSaveSuccess = { navController.popBackStack() }
            )
        }
        composable(
            route = Routes.READER,
            arguments = listOf(navArgument("songId") { type = NavType.StringType })
        ) { backStackEntry ->
            val songId = backStackEntry.arguments?.getString("songId") ?: return@composable
            val readerViewModel: ReaderViewModel = viewModel(
                factory = ReaderViewModel.Factory(songRepository, songId)
            )
            SongReaderScreen(
                viewModel = readerViewModel,
                playlistsViewModel = playlistsViewModel,
                onBackClick = { navController.popBackStack() },
                onEditClick = { navController.navigate(Routes.editSong(songId)) },
                onDeleteSuccess = { navController.popBackStack() }
            )
        }
    }
}

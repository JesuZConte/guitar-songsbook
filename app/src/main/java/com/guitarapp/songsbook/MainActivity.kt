package com.guitarapp.songsbook

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale
import com.google.android.gms.ads.MobileAds
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.guitarapp.songsbook.data.local.ThemeMode
import com.guitarapp.songsbook.data.local.UserPreferences
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.Column
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.guitarapp.songsbook.ui.components.BrassPill
import com.guitarapp.songsbook.ui.theme.LocalLeatherColors
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.LocalContext
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
import androidx.annotation.StringRes
import com.guitarapp.songsbook.R
import com.guitarapp.songsbook.presentation.Routes
import com.guitarapp.songsbook.presentation.screens.AboutScreen
import com.guitarapp.songsbook.presentation.screens.AddSongScreen
import com.guitarapp.songsbook.presentation.screens.CollectionsLandingScreen
import com.guitarapp.songsbook.presentation.screens.PreviewReaderScreen
import com.guitarapp.songsbook.presentation.screens.FavoritesScreen
import com.guitarapp.songsbook.presentation.screens.HomeScreen
import com.guitarapp.songsbook.presentation.screens.PlaylistDetailScreen
import com.guitarapp.songsbook.presentation.screens.SetlistScreen
import com.guitarapp.songsbook.presentation.screens.SettingsScreen
import com.guitarapp.songsbook.presentation.screens.SongReaderScreen
import com.guitarapp.songsbook.presentation.screens.VersionEditorScreen
import com.guitarapp.songsbook.presentation.viewmodel.VersionEditorViewModel
import com.guitarapp.songsbook.presentation.viewmodel.AddSongViewModel
import com.guitarapp.songsbook.presentation.viewmodel.FavoritesViewModel
import com.guitarapp.songsbook.presentation.viewmodel.HomeViewModel
import com.guitarapp.songsbook.presentation.viewmodel.PlaylistsViewModel
import com.guitarapp.songsbook.presentation.viewmodel.ReaderViewModel
import com.guitarapp.songsbook.ui.theme.CancioneroTheme

data class BottomNavItem(
    val route: String,
    @StringRes val labelRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

private val bottomNavItems = listOf(
    BottomNavItem(Routes.HOME, R.string.nav_home, Icons.Filled.Home, Icons.Outlined.Home),
    BottomNavItem(Routes.FAVORITES, R.string.nav_favorites, Icons.Filled.Favorite, Icons.Outlined.FavoriteBorder)
)

class MainActivity : ComponentActivity() {

    var themeMode by mutableStateOf(ThemeMode.SYSTEM)

    private val database by lazy { SongDatabase.getInstance(this) }
    private val songRepository: SongRepository by lazy {
        AssetSongRepository(assets, database.songDao(), database.songVersionDao())
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

    override fun attachBaseContext(newBase: Context) {
        val lang = UserPreferences.getLanguage(newBase)
        val base = if (lang != null) {
            val locale = Locale(lang)
            Locale.setDefault(locale)
            val config = Configuration(newBase.resources.configuration)
            config.setLocale(locale)
            newBase.createConfigurationContext(config)
        } else newBase
        super.attachBaseContext(base)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        MobileAds.initialize(this)
        lifecycleScope.launch {
            songRepository.getSongs() // ensures songs are seeded before playlist cross-refs
            playlistRepository.ensureDefaultCollections()
            playlistsViewModel.loadPlaylists() // refresh after seeding so home screen shows collections immediately
            handleIncomingIntent(intent)
        }
        enableEdgeToEdge()
        themeMode = UserPreferences.getThemeMode(this)
        setContent {
            val darkTheme = when (themeMode) {
                ThemeMode.DARK -> true
                ThemeMode.LIGHT -> false
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
            }
            CancioneroTheme(darkTheme = darkTheme) {
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

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        lifecycleScope.launch { handleIncomingIntent(intent) }
    }

    private suspend fun handleIncomingIntent(intent: Intent?) {
        if (intent?.action != Intent.ACTION_VIEW) return
        val uri = intent.data ?: return
        val json = try {
            withContext(Dispatchers.IO) {
                contentResolver.openInputStream(uri)
                    ?.bufferedReader()
                    ?.use { it.readText() }
            }
        } catch (_: Exception) { null } ?: return
        homeViewModel.importSongFromJson(json)
    }
}

@Composable
private fun GuitarBottomBar(
    currentDestination: NavDestination?,
    onTabSelected: (String) -> Unit
) {
    val c = LocalLeatherColors.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Brush.verticalGradient(listOf(c.navy, c.navyDeep)))
            .stitchedTop(c.stitch)
            .windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
        ) {
            bottomNavItems.forEach { item ->
                val selected = currentDestination?.hierarchy?.any {
                    it.route == item.route
                } == true
                BottomNavItem(
                    item = item,
                    selected = selected,
                    onClick = { if (!selected) onTabSelected(item.route) },
                )
            }
        }
    }
}

@Composable
private fun BottomNavItem(
    item: BottomNavItem,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val c = LocalLeatherColors.current
    Column(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        if (selected) {
            BrassPill(
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    horizontal = 18.dp, vertical = 5.dp
                ),
            ) {
                Icon(
                    imageVector = item.selectedIcon,
                    contentDescription = stringResource(item.labelRes),
                    tint = c.navyDeep,
                )
            }
        } else {
            Box(modifier = Modifier.padding(horizontal = 18.dp, vertical = 5.dp)) {
                Icon(
                    imageVector = item.unselectedIcon,
                    contentDescription = stringResource(item.labelRes),
                    tint = c.creamSoft,
                )
            }
        }
        Text(
            text = stringResource(item.labelRes),
            fontSize = 12.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            fontStyle = if (selected) FontStyle.Normal else FontStyle.Italic,
            color = if (selected) c.brassLight else c.creamSoft,
            letterSpacing = 0.5.sp,
        )
    }
}

private fun Modifier.stitchedTop(color: Color): Modifier =
    this.drawWithContent {
        drawContent()
        val dash = PathEffect.dashPathEffect(floatArrayOf(6f, 4f), 0f)
        drawLine(
            color = color,
            start = Offset(12.dp.toPx(), 4.dp.toPx()),
            end = Offset(size.width - 12.dp.toPx(), 4.dp.toPx()),
            strokeWidth = 1.dp.toPx(),
            pathEffect = dash,
        )
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
    val playlistsUiState by playlistsViewModel.uiState.collectAsState()

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
                playlistsViewModel.loadPlaylists()
            }
            CollectionsLandingScreen(
                playlistsViewModel = playlistsViewModel,
                onAllSongsClick = { navController.navigate(Routes.ALL_SONGS) },
                onCollectionClick = { playlistId ->
                    navController.navigate(Routes.playlistDetail(playlistId))
                },
                onSettingsClick = { navController.navigate(Routes.SETTINGS) }
            )
        }
        composable(Routes.ALL_SONGS) {
            HomeScreen(
                viewModel = homeViewModel,
                playlists = playlistsUiState.playlists,
                onSongClick = { songId -> navController.navigate(Routes.reader(songId)) },
                onEditClick = { songId -> navController.navigate(Routes.editSong(songId)) },
                onAddSongClick = { navController.navigate(Routes.ADD_SONG) },
                showSettings = false,
                onBackClick = { navController.popBackStack() },
                onAddToPlaylist = { songId, playlistId ->
                    playlistsViewModel.addSongToPlaylist(playlistId, songId)
                }
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
                onStartSetlist = { id -> navController.navigate(Routes.setlist(id)) },
                onBackClick = { navController.popBackStack() }
            )
        }
        composable(
            route = Routes.SETLIST,
            arguments = listOf(navArgument("playlistId") { type = NavType.LongType })
        ) { backStackEntry ->
            val playlistId = backStackEntry.arguments?.getLong("playlistId") ?: return@composable
            SetlistScreen(
                playlistId = playlistId,
                viewModel = playlistsViewModel,
                onExit = { navController.popBackStack() }
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
                        addSongViewModel.pendingPreview = preview
                        navController.navigate(Routes.PREVIEW)
                    }
                },
                onSaveSuccess = {
                    navController.popBackStack()
                }
            )
        }
        composable(Routes.PREVIEW) {
            val sourceEntry = navController.previousBackStackEntry
            val sourceViewModel: AddSongViewModel? = sourceEntry?.let {
                viewModel(viewModelStoreOwner = it, factory = AddSongViewModel.Factory(songRepository))
            }
            val previewSong = remember { sourceViewModel?.pendingPreview }
            if (previewSong != null) {
                PreviewReaderScreen(
                    song = previewSong,
                    onBackClick = {
                        sourceViewModel?.pendingPreview = null
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
            route = Routes.ADD_VERSION,
            arguments = listOf(
                navArgument("songId") { type = NavType.StringType },
                navArgument("sourceVersionId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val vSongId = backStackEntry.arguments?.getString("songId") ?: return@composable
            val sourceVersionId = backStackEntry.arguments?.getLong("sourceVersionId") ?: return@composable
            val vm: VersionEditorViewModel = viewModel(
                factory = VersionEditorViewModel.Factory(songRepository, vSongId, null, sourceVersionId)
            )
            VersionEditorScreen(
                viewModel = vm,
                onBackClick = { navController.popBackStack() },
                onSaveSuccess = { navController.popBackStack() }
            )
        }
        composable(
            route = Routes.EDIT_VERSION,
            arguments = listOf(navArgument("versionId") { type = NavType.LongType })
        ) { backStackEntry ->
            val versionId = backStackEntry.arguments?.getLong("versionId") ?: return@composable
            val vm: VersionEditorViewModel = viewModel(
                factory = VersionEditorViewModel.Factory(songRepository, "", versionId, 0L)
            )
            VersionEditorScreen(
                viewModel = vm,
                onBackClick = { navController.popBackStack() },
                onSaveSuccess = { navController.popBackStack() }
            )
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
                        editSongViewModel.pendingPreview = preview
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
            val context = LocalContext.current
            val readerViewModel: ReaderViewModel = viewModel(
                factory = ReaderViewModel.Factory(
                    songRepository,
                    songId,
                    UserPreferences.getFontSize(context),
                    onFontSizePersist = { size -> UserPreferences.setFontSize(context.applicationContext, size) }
                )
            )
            SongReaderScreen(
                viewModel = readerViewModel,
                playlistsViewModel = playlistsViewModel,
                onBackClick = { navController.popBackStack() },
                onEditClick = { versionId -> navController.navigate(Routes.editVersion(versionId)) },
                onDeleteSuccess = { navController.popBackStack() },
                onAddVersionClick = { sid, sourceVersionId ->
                    navController.navigate(Routes.addVersion(sid, sourceVersionId))
                },
                onEditVersionClick = { versionId ->
                    navController.navigate(Routes.editVersion(versionId))
                }
            )
        }
    }
}

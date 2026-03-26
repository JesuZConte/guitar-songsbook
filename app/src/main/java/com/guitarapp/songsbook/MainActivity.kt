package com.guitarapp.songsbook

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.guitarapp.songsbook.data.local.SongDatabase
import com.guitarapp.songsbook.data.repository.AssetSongRepository
import com.guitarapp.songsbook.data.repository.SongRepository
import com.guitarapp.songsbook.presentation.screens.FavoritesScreen
import com.guitarapp.songsbook.presentation.screens.HomeScreen
import com.guitarapp.songsbook.presentation.screens.SongReaderScreen
import com.guitarapp.songsbook.presentation.viewmodel.FavoritesViewModel
import com.guitarapp.songsbook.presentation.viewmodel.HomeViewModel
import com.guitarapp.songsbook.presentation.viewmodel.ReaderViewModel
import com.guitarapp.songsbook.ui.theme.GuitarSongsbookTheme

data class BottomNavItem(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

class MainActivity : ComponentActivity() {

    private val database by lazy { SongDatabase.getInstance(this) }
    private val songRepository: SongRepository by lazy {
        AssetSongRepository(assets, database.songDao())
    }

    private val homeViewModel: HomeViewModel by viewModels {
        HomeViewModel.Factory(songRepository)
    }

    private val favoritesViewModel: FavoritesViewModel by viewModels {
        FavoritesViewModel.Factory(songRepository)
    }

    private val bottomNavItems = listOf(
        BottomNavItem("home", "Home", Icons.Filled.Home, Icons.Outlined.Home),
        BottomNavItem("favorites", "Favorites", Icons.Filled.Favorite, Icons.Outlined.FavoriteBorder)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GuitarSongsbookTheme {
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
                                NavigationBar {
                                    bottomNavItems.forEach { item ->
                                        val selected = currentDestination?.hierarchy?.any {
                                            it.route == item.route
                                        } == true

                                        NavigationBarItem(
                                            selected = selected,
                                            onClick = {
                                                if (!selected) {
                                                    navController.navigate(item.route) {
                                                        popUpTo("home") { saveState = true }
                                                        launchSingleTop = true
                                                        restoreState = true
                                                    }
                                                }
                                            },
                                            icon = {
                                                Icon(
                                                    imageVector = if (selected) item.selectedIcon
                                                    else item.unselectedIcon,
                                                    contentDescription = item.label
                                                )
                                            },
                                            label = { Text(item.label) }
                                        )
                                    }
                                }
                            }
                        }
                    ) { paddingValues ->
                        NavHost(
                            navController = navController,
                            startDestination = "home",
                            modifier = Modifier.padding(paddingValues)
                        ) {
                            composable("home") {
                                homeViewModel.refreshSongs()
                                HomeScreen(
                                    viewModel = homeViewModel,
                                    onSongClick = { songId ->
                                        navController.navigate("reader/$songId")
                                    }
                                )
                            }
                            composable("favorites") {
                                favoritesViewModel.loadFavorites()
                                FavoritesScreen(
                                    viewModel = favoritesViewModel,
                                    onSongClick = { songId ->
                                        navController.navigate("reader/$songId")
                                    }
                                )
                            }
                            composable(
                                route = "reader/{songId}",
                                arguments = listOf(
                                    navArgument("songId") { type = NavType.StringType }
                                )
                            ) { backStackEntry ->
                                val songId = backStackEntry.arguments?.getString("songId") ?: return@composable
                                val readerViewModel: ReaderViewModel = viewModel(
                                    factory = ReaderViewModel.Factory(songRepository, songId)
                                )
                                SongReaderScreen(
                                    viewModel = readerViewModel,
                                    onBackClick = { navController.popBackStack() }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
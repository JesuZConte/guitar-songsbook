package com.guitarapp.songsbook

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.guitarapp.songsbook.data.local.SongDatabase
import com.guitarapp.songsbook.data.repository.AssetSongRepository
import com.guitarapp.songsbook.data.repository.SongRepository
import com.guitarapp.songsbook.presentation.screens.HomeScreen
import com.guitarapp.songsbook.presentation.screens.SongDetailScreen
import com.guitarapp.songsbook.presentation.viewmodel.HomeViewModel
import com.guitarapp.songsbook.presentation.viewmodel.SongDetailViewModel
import com.guitarapp.songsbook.ui.theme.GuitarSongsbookTheme

class MainActivity : ComponentActivity() {

    private val database by lazy { SongDatabase.getInstance(this) }
    private val songRepository: SongRepository by lazy {
        AssetSongRepository(assets, database.songDao())
    }

    private val homeViewModel: HomeViewModel by viewModels {
        HomeViewModel.Factory(songRepository)
    }

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

                    NavHost(
                        navController = navController,
                        startDestination = "home"
                    ) {
                        composable("home") {
                            HomeScreen(
                                viewModel = homeViewModel,
                                onSongClick = { songId ->
                                    navController.navigate("detail/$songId")
                                }
                            )
                        }
                        composable(
                            route = "detail/{songId}",
                            arguments = listOf(
                                navArgument("songId") { type = NavType.StringType }
                            )
                        ) { backStackEntry ->
                            val songId = backStackEntry.arguments?.getString("songId") ?: return@composable
                            val detailViewModel: SongDetailViewModel = viewModel(
                                factory = SongDetailViewModel.Factory(songRepository, songId)
                            )
                            SongDetailScreen(
                                viewModel = detailViewModel,
                                onBackClick = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}
package com.mohamed.calmplayer.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.mohamed.calmplayer.data.Song
import com.mohamed.calmplayer.ui.screens.HomeScreen
import com.mohamed.calmplayer.ui.screens.LibraryScreen
import com.mohamed.calmplayer.ui.screens.SearchScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mohamed.calmplayer.domain.MusicViewModel

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Search : Screen("search")
    object Library : Screen("library")
    object Settings : Screen("settings")
    object Player : Screen("player")
}

@Composable
fun CalmMusicNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    onSongClick: (Song, List<Song>) -> Unit,
    onSettingsClick: () -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = modifier
    ) {
        composable(Screen.Home.route) {
            HomeScreen(onSettingsClick = onSettingsClick, onSongClick = onSongClick)
        }
        composable(Screen.Search.route) {
            SearchScreen(onSongClick = onSongClick)
        }
        composable(Screen.Library.route) {
            LibraryScreen(onSongClick = onSongClick, onSettingsClick = onSettingsClick)
        }
        composable(Screen.Settings.route) {
            com.mohamed.calmplayer.ui.screens.SettingsScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
        composable(Screen.Player.route) {
            val musicVm: MusicViewModel = viewModel()
            val song by musicVm.currentSong.collectAsState()
            val isPlaying by musicVm.isPlaying.collectAsState()

            com.mohamed.calmplayer.ui.components.PlayerSheet(
                song = song,
                isPlaying = isPlaying,
                position = 0L,
                duration = 0L,
                onPositionChange = { musicVm.seekTo(it) },
                onPlayPause = { musicVm.togglePlayPause() },
                onSkipNext = { musicVm.skipNext() },
                onSkipPrevious = { musicVm.skipPrevious() },
                onDismiss = { navController.popBackStack() },
                visible = true
            )
        }
    }
}

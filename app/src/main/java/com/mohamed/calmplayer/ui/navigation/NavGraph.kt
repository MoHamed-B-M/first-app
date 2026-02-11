package com.mohamed.calmplayer.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.mohamed.calmplayer.data.Song
import com.mohamed.calmplayer.ui.screens.HomeScreen
import com.mohamed.calmplayer.ui.screens.LibraryScreen
import com.mohamed.calmplayer.ui.screens.SearchScreen

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
        composable(
            route = Screen.Home.route,
            enterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow))
            },
            exitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow))
            }
        ) {
            HomeScreen(onSettingsClick = onSettingsClick, onSongClick = onSongClick)
        }
        
        composable(
            route = Screen.Search.route,
            enterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Up, spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow))
            },
            exitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Down, spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow))
            }
        ) {
            SearchScreen(onSongClick = onSongClick)
        }
        
        composable(
            route = Screen.Library.route,
            enterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow))
            },
            exitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow))
            }
        ) {
            LibraryScreen(onSongClick = onSongClick, onSettingsClick = onSettingsClick)
        }
        
        composable(
            route = Screen.Settings.route,
            enterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Up, spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow))
            },
            exitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Down, spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow))
            }
        ) {
            com.mohamed.calmplayer.ui.screens.SettingsScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.Player.route,
            enterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Up, spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow))
            },
            exitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Down, spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow))
            }
        ) {
            val playerViewModel: com.mohamed.calmplayer.domain.PlayerViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
            val currentSong by playerViewModel.currentSong.collectAsState()
            val isPlaying by playerViewModel.isPlaying.collectAsState()
            val position by playerViewModel.position.collectAsState()
            val duration by playerViewModel.duration.collectAsState()

            // Handle the back button to navigate back from the player
            com.mohamed.calmplayer.ui.components.PlayerSheet(
                song = currentSong,
                isPlaying = isPlaying,
                position = position,
                duration = duration,
                onPositionChange = { playerViewModel.seekTo(it) },
                onPlayPause = { playerViewModel.togglePlayPause() },
                onSkipNext = { playerViewModel.skipNext() },
                onSkipPrevious = { playerViewModel.skipPrevious() },
                onDismiss = { navController.popBackStack() },
                visible = true,
                sharedTransitionScope = null, // Disable shared transition for now to fix stability
                animatedVisibilityScope = null
            )
        }
    }
}

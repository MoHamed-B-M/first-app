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
    onSongClick: (Song) -> Unit,
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
    }
}

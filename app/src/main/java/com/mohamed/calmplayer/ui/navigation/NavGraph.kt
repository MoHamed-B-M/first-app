package com.mohamed.calmplayer.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
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
    object Player : Screen("player")
}

@Composable
fun CalmMusicNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    onSongClick: (Song) -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = modifier
    ) {
        composable(
            route = Screen.Home.route,
            enterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(500))
            },
            exitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(500))
            }
        ) {
            HomeScreen()
        }
        
        composable(
            route = Screen.Search.route,
            enterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Up, tween(500))
            },
            exitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Down, tween(500))
            }
        ) {
            SearchScreen()
        }
        
        composable(
            route = Screen.Library.route,
            enterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(500))
            },
            exitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(500))
            }
        ) {
            LibraryScreen(onSongClick = onSongClick)
        }
        
        // Player route if needed as a full screen, but likely it will be a bottom sheet
    }
}

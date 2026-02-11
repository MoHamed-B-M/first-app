package com.mohamed.calmplayer

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import androidx.compose.ui.text.style.TextOverflow
import androidx.media3.common.MediaItem
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import android.content.ComponentName
import com.mohamed.calmplayer.data.MediaLibraryHelper
import com.mohamed.calmplayer.data.Song
import com.mohamed.calmplayer.service.PlaybackService
import com.mohamed.calmplayer.ui.components.PlayerSheet
import com.mohamed.calmplayer.ui.navigation.CalmMusicNavHost
import com.mohamed.calmplayer.ui.navigation.Screen
import com.mohamed.calmplayer.ui.theme.CalmMusicTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val settingsViewModel: com.mohamed.calmplayer.domain.SettingsViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
            val themeConfig by settingsViewModel.themeState.collectAsState()
            val darkTheme = when (themeConfig) {
                com.mohamed.calmplayer.data.ThemeConfig.LIGHT -> false
                com.mohamed.calmplayer.data.ThemeConfig.DARK -> true
                com.mohamed.calmplayer.data.ThemeConfig.SYSTEM -> androidx.compose.foundation.isSystemInDarkTheme()
            }

            CalmMusicTheme(darkTheme = darkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen()
                }
            }
        }
    }
}

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val context = LocalContext.current
    
    var currentSong by remember { mutableStateOf<Song?>(null) }
    var isPlaying by remember { mutableStateOf(false) }
    var showPlayerSheet by remember { mutableStateOf(false) }
    
    var controller by remember { mutableStateOf<MediaController?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted -> }
    )

    LaunchedEffect(Unit) {
        val sessionToken = SessionToken(context, ComponentName(context, PlaybackService::class.java))
        val controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
        controllerFuture.addListener({
            controller = controllerFuture.get()
        }, MoreExecutors.directExecutor())
        
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
            permissionLauncher.launch(permission)
        }
    }

    // Synchronize isPlaying state from controller
    LaunchedEffect(controller) {
        controller?.addListener(object : androidx.media3.common.Player.Listener {
            override fun onIsPlayingChanged(isPlayingChanged: Boolean) {
                isPlaying = isPlayingChanged
            }
        })
    }

    @OptIn(ExperimentalSharedTransitionApi::class)
    SharedTransitionLayout {
        Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            bottomBar = {
                Column {
                    AnimatedVisibility(
                        visible = currentSong != null && !showPlayerSheet,
                        enter = slideInVertically(initialOffsetY = { it }),
                        exit = slideOutVertically(targetOffsetY = { it })
                    ) {
                        currentSong?.let { song ->
                            MiniPlayer(
                                song = song,
                                isPlaying = isPlaying,
                                onPlayPause = { 
                                    if (isPlaying) controller?.pause() else controller?.play()
                                },
                                onSkipNext = { controller?.seekToNext() },
                                onClick = { showPlayerSheet = true },
                                sharedTransitionScope = this@SharedTransitionLayout,
                                animatedContentScope = this@AnimatedVisibility
                            )
                        }
                    }
                    
                    NavigationBar {
                        val navBackStackEntry by navController.currentBackStackEntryAsState()
                        val currentRoute = navBackStackEntry?.destination?.route

                        NavigationBarItem(
                            icon = { Icon(Icons.Filled.Home, contentDescription = "Home") },
                            label = { Text("Home") },
                            selected = currentRoute == Screen.Home.route,
                            onClick = {
                                navController.navigate(Screen.Home.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                        NavigationBarItem(
                            icon = { Icon(Icons.Filled.Search, contentDescription = "Search") },
                            label = { Text("Search") },
                            selected = currentRoute == Screen.Search.route,
                            onClick = {
                                navController.navigate(Screen.Search.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                        NavigationBarItem(
                            icon = { Icon(Icons.Filled.LibraryMusic, contentDescription = "Library") },
                            label = { Text("Library") },
                            selected = currentRoute == Screen.Library.route,
                            onClick = {
                                navController.navigate(Screen.Library.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        ) { innerPadding ->
            CalmMusicNavHost(
                navController = navController,
                modifier = Modifier.padding(innerPadding),
                onSongClick = { song ->
                    currentSong = song
                    showPlayerSheet = true
                    controller?.let {
                        val helper = MediaLibraryHelper(context)
                        // Note: ideally blocked folders should be passed here too if you want to skip blocked songs in queue
                        val allSongs = helper.getAllSongs() 
                        val startIndex = allSongs.indexOfFirst { it.id == song.id }
                        
                        val mediaItems = allSongs.map {
                            MediaItem.Builder()
                                .setMediaId(it.id.toString())
                                .setUri(it.uri)
                                .build()
                        }
                        
                        it.setMediaItems(mediaItems, startIndex, 0)
                        it.prepare()
                        it.play()
                    }
                },
                onSettingsClick = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }

        var position by remember { mutableLongStateOf(0L) }
        var duration by remember { mutableLongStateOf(0L) }

        LaunchedEffect(isPlaying) {
            if (isPlaying) {
                while (true) {
                    position = controller?.currentPosition ?: 0L
                    duration = controller?.duration ?: 0L
                    kotlinx.coroutines.delay(1000)
                }
            }
        }

        AnimatedContent(
            targetState = showPlayerSheet,
            label = "playerTransition",
            transitionSpec = {
                if (targetState) {
                    slideInVertically { it } + fadeIn() togetherWith
                            fadeOut()
                } else {
                    fadeIn() togetherWith
                            slideOutVertically { it } + fadeOut()
                }
            }
        ) { targetShowPlayerSheet ->
            if (targetShowPlayerSheet) {
                PlayerSheet(
                    song = currentSong,
                    isPlaying = isPlaying,
                    position = position,
                    duration = duration,
                    onPositionChange = { newPos -> controller?.seekTo(newPos) },
                    onPlayPause = { 
                        if (isPlaying) controller?.pause() else controller?.play()
                    },
                    onSkipNext = { controller?.seekToNext() },
                    onSkipPrevious = { controller?.seekToPrevious() },
                    onDismiss = { showPlayerSheet = false },
                    visible = true,
                    sharedTransitionScope = this@SharedTransitionLayout,
                    animatedContentScope = this@AnimatedContent
                )
            }
        }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun MiniPlayer(
    song: Song,
    isPlaying: Boolean,
    onPlayPause: () -> Unit,
    onSkipNext: () -> Unit,
    onClick: () -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope
) {
    with(sharedTransitionScope) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .height(64.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        tonalElevation = 4.dp
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = song.albumArtUri,
                contentDescription = null,
                modifier = Modifier
                    .size(48.dp)
                    .sharedElement(
                        rememberSharedContentState(key = "album_art_${song.id}"),
                        animatedVisibilityScope = animatedContentScope
                    )
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
            
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp)
            ) {
                Text(
                    text = song.title,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1
                )
                Text(
                    text = song.artist,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1
                )
            }
            
            IconButton(onClick = onPlayPause) {
                Icon(
                    imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    contentDescription = null
                )
            }
            
            IconButton(onClick = onSkipNext) {
                Icon(Icons.Filled.SkipNext, contentDescription = null)
            }
        }
    }
    }
}
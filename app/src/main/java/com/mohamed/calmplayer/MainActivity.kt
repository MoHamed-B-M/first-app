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
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LoadingIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.mohamed.calmplayer.data.MediaLibraryHelper
import com.mohamed.calmplayer.data.Song
import com.mohamed.calmplayer.service.PlaybackService
import com.mohamed.calmplayer.ui.components.PlayerSheet
import com.mohamed.calmplayer.ui.components.getSquircleShape
import com.mohamed.calmplayer.ui.navigation.CalmMusicNavHost
import com.mohamed.calmplayer.ui.navigation.Screen
import com.mohamed.calmplayer.ui.theme.CalmMusicTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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
                    MainScreen(settingsViewModel = settingsViewModel)
                }
            }
        }
    }
}

// ─── Welcome / Intro Screen ────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun WelcomeScreen(
    onFolderSelected: () -> Unit,
    onSelectFolder: () -> Unit,
    isScanning: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.padding(48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // App icon placeholder
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(getSquircleShape())
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.MusicNote,
                    contentDescription = null,
                    modifier = Modifier.size(60.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Welcome to",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "CalmPlayer",
                style = MaterialTheme.typography.displayMedium.copy(
                    fontWeight = FontWeight.Black
                ),
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Your expressive music experience starts here.\nSelect your music folder to begin.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(48.dp))

            if (isScanning) {
                // Material 3 Expressive loading indicator
                LoadingIndicator(
                    modifier = Modifier.size(48.dp),
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Scanning your library...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Button(
                    onClick = onSelectFolder,
                    shape = RoundedCornerShape(28.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        Icons.Filled.FolderOpen,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Select Music Folder",
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(onClick = onFolderSelected) {
                    Text(
                        text = "Skip for now",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

// ─── Main Screen ───────────────────────────────────────────────────────────────
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun MainScreen(
    settingsViewModel: com.mohamed.calmplayer.domain.SettingsViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    playerViewModel: com.mohamed.calmplayer.domain.PlayerViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val context = LocalContext.current
    val musicFolderUri by settingsViewModel.musicFolderUri.collectAsState()
    var hasCompletedIntro by remember { mutableStateOf(false) }
    var isScanning by remember { mutableStateOf(false) }

    // Permission handling
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { }
    )

    LaunchedEffect(Unit) {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
        if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
            permissionLauncher.launch(permission)
        }
    }

    // Folder picker using OpenDocumentTree
    val folderPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        uri?.let {
            // Persist permission
            val flags = android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
            context.contentResolver.takePersistableUriPermission(it, flags)
            settingsViewModel.setMusicFolderUri(it.toString())

            // Simulate scanning with loading indicator
            isScanning = true
        }
    }

    // When scanning starts, wait briefly then complete
    LaunchedEffect(isScanning) {
        if (isScanning) {
            // Let the loading indicator show for a moment while library indexes
            withContext(Dispatchers.IO) {
                val helper = MediaLibraryHelper(context)
                helper.getAllSongs() // Pre-cache the scan
            }
            isScanning = false
            hasCompletedIntro = true
        }
    }

    // Determine if we should show intro
    val showIntro = musicFolderUri == null && !hasCompletedIntro

    AnimatedContent(
        targetState = showIntro,
        label = "introTransition",
        transitionSpec = {
            fadeIn(spring(stiffness = Spring.StiffnessLow)) togetherWith
                    fadeOut(spring(stiffness = Spring.StiffnessLow))
        }
    ) { shouldShowIntro ->
        if (shouldShowIntro) {
            WelcomeScreen(
                onFolderSelected = { hasCompletedIntro = true },
                onSelectFolder = { folderPickerLauncher.launch(null) },
                isScanning = isScanning
            )
        } else {
            MainContent(
                playerViewModel = playerViewModel,
                settingsViewModel = settingsViewModel
            )
        }
    }
}

// ─── Main Content (Post-Intro) ─────────────────────────────────────────────────
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun MainContent(
    playerViewModel: com.mohamed.calmplayer.domain.PlayerViewModel,
    settingsViewModel: com.mohamed.calmplayer.domain.SettingsViewModel
) {
    val navController = rememberNavController()
    val context = LocalContext.current

    val currentSong by playerViewModel.currentSong.collectAsState()
    val isPlaying by playerViewModel.isPlaying.collectAsState()
    val position by playerViewModel.position.collectAsState()
    val duration by playerViewModel.duration.collectAsState()

    SharedTransitionLayout {
        Box(modifier = Modifier.fillMaxSize()) {
            Scaffold(
                bottomBar = {
                    Column {
                        val navBackStackEntry by navController.currentBackStackEntryAsState()
                        val currentRoute = navBackStackEntry?.destination?.route
                        
                        // Only show MiniPlayer if we are NOT on the Player screen
                        AnimatedVisibility(
                            visible = currentSong != null && currentRoute != Screen.Player.route,
                            enter = slideInVertically(initialOffsetY = { it }),
                            exit = slideOutVertically(targetOffsetY = { it })
                        ) {
                            currentSong?.let { song ->
                                MiniPlayer(
                                    song = song,
                                    isPlaying = isPlaying,
                                    onPlayPause = { playerViewModel.togglePlayPause() },
                                    onSkipNext = { playerViewModel.skipNext() },
                                    onExpand = { 
                                        navController.navigate(Screen.Player.route) {
                                            launchSingleTop = true
                                        }
                                    },
                                    onDismiss = { /* Mini player stays */ },
                                    sharedTransitionScope = this@SharedTransitionLayout,
                                    animatedVisibilityScope = this
                                )
                            }
                        }

                        NavigationBar {
                            NavigationBarItem(
                                icon = { Icon(Icons.Filled.Home, "Home") },
                                label = { Text("Home") },
                                selected = currentRoute == Screen.Home.route,
                                onClick = {
                                    navController.navigate(Screen.Home.route) {
                                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            )
                            NavigationBarItem(
                                icon = { Icon(Icons.Filled.Search, "Search") },
                                label = { Text("Search") },
                                selected = currentRoute == Screen.Search.route,
                                onClick = {
                                    navController.navigate(Screen.Search.route) {
                                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            )
                            NavigationBarItem(
                                icon = { Icon(Icons.Filled.LibraryMusic, "Library") },
                                label = { Text("Library") },
                                selected = currentRoute == Screen.Library.route,
                                onClick = {
                                    navController.navigate(Screen.Library.route) {
                                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
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
                    onSongClick = { song, list ->
                        // Fix: Use the provided song list instead of re-scanning for performance
                        val finalSongList = list.ifEmpty { 
                            // Fallback if list is empty (e.g. from Home where we only have a slice)
                            val helper = MediaLibraryHelper(context)
                            helper.getAllSongs()
                        }
                        playerViewModel.playSong(song, finalSongList)
                        navController.navigate(Screen.Player.route) {
                            launchSingleTop = true
                        }
                    },
                    onSettingsClick = {
                        navController.navigate(Screen.Settings.route)
                    }
                )
            }
        }
    }
}

// ─── MiniPlayer with Swipe Gestures ─────────────────────────────────────────────
@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MiniPlayer(
    song: Song,
    isPlaying: Boolean,
    onPlayPause: () -> Unit,
    onSkipNext: () -> Unit,
    onExpand: () -> Unit,
    onDismiss: () -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope
) {
    // Track cumulative drag for gesture detection
    var dragOffset by remember { mutableFloatStateOf(0f) }

    // Bouncy animation using MaterialTheme.motionScheme.slowSpatialSpec()
    val animatedOffset by animateFloatAsState(
        targetValue = dragOffset,
        animationSpec = MaterialTheme.motionScheme.slowSpatialSpec(),
        label = "miniPlayerDrag"
    )

    with(sharedTransitionScope) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp)
                .height(64.dp)
                .graphicsLayer {
                    translationY = animatedOffset
                }
                .pointerInput(Unit) {
                    detectVerticalDragGestures(
                        onDragStart = { dragOffset = 0f },
                        onDragEnd = {
                            if (dragOffset < -80f) {
                                // Swipe Up → Expand to full player
                                onExpand()
                            }
                            // Reset drag offset
                            dragOffset = 0f
                        },
                        onDragCancel = { dragOffset = 0f },
                        onVerticalDrag = { _, dragAmount ->
                            // Only track upward swipe (negative = up)
                            dragOffset = (dragOffset + dragAmount).coerceIn(-200f, 0f)
                        }
                    )
                }
                .clickable(onClick = onExpand),
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
                            animatedVisibilityScope = animatedVisibilityScope
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
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = song.artist,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                IconButton(onClick = onPlayPause) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play"
                    )
                }

                IconButton(onClick = onSkipNext) {
                    Icon(Icons.Filled.SkipNext, contentDescription = "Next")
                }
            }
        }
    }
}
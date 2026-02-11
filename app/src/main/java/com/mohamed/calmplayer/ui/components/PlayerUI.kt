package com.mohamed.calmplayer.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ConnectedButton
import androidx.compose.material3.ConnectedButtonGroup
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.runtime.Composable
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.mohamed.calmplayer.data.Song

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun PlayerSheet(
    song: Song?,
    isPlaying: Boolean,
    position: Long,
    duration: Long,
    onPositionChange: (Long) -> Unit,
    onPlayPause: () -> Unit,
    onSkipNext: () -> Unit,
    onSkipPrevious: () -> Unit,
    onDismiss: () -> Unit,
    visible: Boolean,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope
) {
    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(initialOffsetY = { it }),
        exit = slideOutVertically(targetOffsetY = { it })
    ) {
        if (song != null) {
            FullPlayerContent(
                song = song, 
                isPlaying = isPlaying, 
                position = position,
                duration = duration,
                onPositionChange = onPositionChange,
                onPlayPause = onPlayPause, 
                onSkipNext = onSkipNext, 
                onSkipPrevious = onSkipPrevious, 
                onCollapse = onDismiss,
                sharedTransitionScope = sharedTransitionScope,
                animatedContentScope = animatedContentScope
            )
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun FullPlayerContent(
    song: Song,
    isPlaying: Boolean,
    position: Long,
    duration: Long,
    onPositionChange: (Long) -> Unit,
    onPlayPause: () -> Unit,
    onSkipNext: () -> Unit,
    onSkipPrevious: () -> Unit,
    onCollapse: () -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope
) {
    with(sharedTransitionScope) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        // Blurred Background
        AsyncImage(
            model = song.albumArtUri,
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .alpha(0.3f)
                .blur(35.dp),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            
            // Drag Handle
            Box(
                modifier = Modifier
                    .width(40.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                    .clickable { onCollapse() }
            )

            Spacer(modifier = Modifier.height(16.dp))
            
            // Handle / Collapse
            IconButton(
                onClick = onCollapse,
                modifier = Modifier.align(Alignment.Start)
            ) {
                Icon(Icons.Filled.KeyboardArrowDown, contentDescription = "Collapse", tint = MaterialTheme.colorScheme.onSurface)
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Large Album Art
            val albumArtMorph by animateFloatAsState(
                targetValue = 1f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow),
                label = "albumArtMorph"
            )
            val albumArtShape = MorphingShape(albumArtMorph)

            AsyncImage(
                model = song.albumArtUri,
                contentDescription = null,
                modifier = Modifier
                    .size(320.dp)
                    .sharedElement(
                        rememberSharedContentState(key = "album_art_${song.id}"),
                        animatedVisibilityScope = animatedContentScope
                    )
                    .clip(albumArtShape)
                    .border(1.dp, Color.White.copy(alpha = 0.1f), albumArtShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentScale = ContentScale.Crop
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // Title & Artist
            Column(
                horizontalAlignment = Alignment.Start,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = song.title,
                    style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = song.artist,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Expressive Slider
            val progress = remember(position, duration) {
                if (duration > 0) position.toFloat() / duration.toFloat() else 0f
            }
            
            ExpressiveSlider(
                progress = progress,
                onValueChange = { newProgress ->
                    onPositionChange((newProgress * duration).toLong())
                }
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(formatTime(position), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(formatTime(duration), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // Controls
            OptIn(ExperimentalMaterial3ExpressiveApi::class) {
                ConnectedButtonGroup {
                    IconButton(
                        onClick = onSkipPrevious,
                        modifier = Modifier
                            .size(72.dp)
                            .connectedButton()
                    ) {
                        Icon(Icons.Filled.SkipPrevious, null, modifier = Modifier.size(32.dp), tint = MaterialTheme.colorScheme.onSurface)
                    }
                    
                    SquircleButton(
                        onClick = onPlayPause,
                        modifier = Modifier
                            .size(96.dp)
                            .connectedButton(),
                        isPlaying = isPlaying,
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    
                    IconButton(
                        onClick = onSkipNext,
                        modifier = Modifier
                            .size(72.dp)
                            .connectedButton()
                    ) {
                        Icon(Icons.Filled.SkipNext, null, modifier = Modifier.size(32.dp), tint = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}
}

private fun formatTime(ms: Long): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%d:%02d", minutes, seconds)
}

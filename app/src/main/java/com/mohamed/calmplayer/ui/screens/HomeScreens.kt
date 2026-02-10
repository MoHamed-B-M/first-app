package com.mohamed.calmplayer.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.mohamed.calmplayer.data.MediaLibraryHelper
import com.mohamed.calmplayer.data.Song
import com.mohamed.calmplayer.ui.components.SquircleButton
import com.mohamed.calmplayer.ui.components.SquircleShape
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun LibraryScreen(
    onSongClick: (Song) -> Unit
) {
    val context = LocalContext.current
    val helper = remember { MediaLibraryHelper(context) }
    var songs by remember { mutableStateOf<List<Song>>(emptyList()) }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            songs = helper.getAllSongs()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Text(
            text = "Library",
            style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(text = "Songs", selected = true)
            FilterChip(text = "Albums", selected = false)
            FilterChip(text = "Artists", selected = false)
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            items(songs) { song ->
                SongItem(song = song, onClick = { onSongClick(song) })
            }
        }
    }
}

@Composable
fun FilterChip(text: String, selected: Boolean) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(
                if (selected) MaterialTheme.colorScheme.primaryContainer 
                else MaterialTheme.colorScheme.surfaceVariant
            )
            .padding(horizontal = 20.dp, vertical = 10.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer 
                    else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun SongItem(song: Song, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = song.albumArtUri,
                contentDescription = null,
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.secondaryContainer),
                contentScale = ContentScale.Crop
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = song.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = song.artist,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Icon(
                imageVector = Icons.Default.MusicNote,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun HomeScreen() {
    val context = LocalContext.current
    val helper = remember { MediaLibraryHelper(context) }
    var songs by remember { mutableStateOf<List<Song>>(emptyList()) }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            songs = helper.getAllSongs().shuffled().take(3)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(48.dp))
        
        Text(
            text = "Your\nMix",
            style = MaterialTheme.typography.displayLarge.copy(
                fontWeight = FontWeight.Black,
                fontSize = 72.sp,
                lineHeight = 72.sp
            ),
            modifier = Modifier.align(Alignment.Start)
        )
        
        Text(
            text = "Today's Mix for you",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.align(Alignment.Start).padding(top = 8.dp)
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        
        Box(modifier = Modifier.fillMaxWidth().height(300.dp)) {
            if (songs.isNotEmpty()) {
                AsyncImage(
                    model = songs[0].albumArtUri,
                    contentDescription = null,
                    modifier = Modifier
                        .size(240.dp)
                        .align(Alignment.Center)
                        .clip(SquircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentScale = ContentScale.Crop
                )
                
                if (songs.size > 1) {
                    AsyncImage(
                        model = songs[1].albumArtUri,
                        contentDescription = null,
                        modifier = Modifier
                            .size(100.dp)
                            .offset(x = (-100).dp, y = (-80).dp)
                            .align(Alignment.Center)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.secondaryContainer),
                        contentScale = ContentScale.Crop
                    )
                }
                
                if (songs.size > 2) {
                    AsyncImage(
                        model = songs[2].albumArtUri,
                        contentDescription = null,
                        modifier = Modifier
                            .size(80.dp)
                            .offset(x = 120.dp, y = 100.dp)
                            .align(Alignment.Center)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.tertiaryContainer),
                        contentScale = ContentScale.Crop
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .align(Alignment.Center)
                        .clip(SquircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                )
            }
            
            SquircleButton(
                onClick = { /* Play Mix */ },
                modifier = Modifier
                    .size(80.dp)
                    .align(Alignment.BottomEnd)
                    .offset(y = (-20).dp),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Icon(
                    Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@Composable
fun SearchScreen() {
    Box(
        modifier = Modifier.fillMaxSize(), 
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Rounded.Search, 
            contentDescription = "Search",
            modifier = Modifier.size(64.dp)
        )
    }
}
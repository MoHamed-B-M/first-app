package com.mohamed.calmplayer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.mohamed.calmplayer.domain.PlayerViewModel
import com.mohamed.calmplayer.ui.components.AudioPlayer
import com.mohamed.calmplayer.ui.theme.CalmMusicTheme

class MainActivity : ComponentActivity() {
    
    // Using viewModels delegate from activity-ktx
    private val viewModel: PlayerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CalmMusicTheme {
                val isPlaying by viewModel.isPlaying.collectAsState()
                
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = MaterialTheme.colorScheme.background
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primaryContainer,
                                        MaterialTheme.colorScheme.tertiaryContainer
                                    )
                                )
                            )
                            .padding(innerPadding),
                        contentAlignment = Alignment.Center
                    ) {
                        AudioPlayer(
                            isPlaying = isPlaying,
                            onPlayPause = { viewModel.togglePlayPause() }
                        )
                    }
                }
            }
        }
    }
}


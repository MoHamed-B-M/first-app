package com.mohamed.calmplayer.domain

import android.app.Application
import android.content.ComponentName
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import com.mohamed.calmplayer.data.Song
import com.mohamed.calmplayer.service.PlaybackService
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PlayerViewModel(application: Application) : AndroidViewModel(application) {

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying = _isPlaying.asStateFlow()

    private val _currentSong = MutableStateFlow<Song?>(null)
    val currentSong = _currentSong.asStateFlow()

    private val _position = MutableStateFlow(0L)
    val position = _position.asStateFlow()

    private val _duration = MutableStateFlow(0L)
    val duration = _duration.asStateFlow()

    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var controller: MediaController? = null

    private var sleepTimerJob: Job? = null
    private val _sleepTimerActive = MutableStateFlow(false)
    val sleepTimerActive = _sleepTimerActive.asStateFlow()

    init {
        val sessionToken = SessionToken(application, ComponentName(application, PlaybackService::class.java))
        controllerFuture = MediaController.Builder(application, sessionToken).buildAsync()
        controllerFuture?.addListener({
            controller = controllerFuture?.get()
            setupController()
        }, MoreExecutors.directExecutor())
        
        startProgressUpdate()
    }

    private fun setupController() {
        controller?.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _isPlaying.value = isPlaying
            }
            
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                // Update current song from media item if needed
                // This requires mapping media item ID back to Song
            }
        })
    }

    private fun startProgressUpdate() {
        viewModelScope.launch {
            while (true) {
                controller?.let {
                    _position.value = it.currentPosition
                    _duration.value = it.duration
                }
                delay(500)
            }
        }
    }

    fun playSong(song: Song, allSongs: List<Song>) {
        _currentSong.value = song
        controller?.let {
            val startIndex = allSongs.indexOfFirst { it.id == song.id }
            val mediaItems = allSongs.map {
                MediaItem.Builder()
                    .setMediaId(it.id.toString())
                    .setUri(it.uri)
                    .build()
            }
            it.setMediaItems(mediaItems, if (startIndex != -1) startIndex else 0, 0)
            it.prepare()
            it.play()
        }
    }

    fun togglePlayPause() {
        controller?.let {
            if (it.isPlaying) it.pause() else it.play()
        }
    }

    fun skipNext() {
        controller?.seekToNext()
    }

    fun skipPrevious() {
        controller?.seekToPrevious()
    }

    fun seekTo(pos: Long) {
        controller?.seekTo(pos)
    }

    // AI Sleep Timer: Fade volume from 1.0 to 0.0 over final 5 minutes
    fun startSleepTimer(minutes: Int) {
        sleepTimerJob?.cancel()
        _sleepTimerActive.value = true
        sleepTimerJob = viewModelScope.launch {
            val totalSeconds = minutes * 60
            val fadeStartSeconds = (totalSeconds - 300).coerceAtLeast(0)
            
            delay(fadeStartSeconds * 1000L)
            
            // Fading over 5 minutes (300 seconds)
            val fadeSteps = 100
            val stepDelay = 3000L // 300 / 100 = 3 seconds per step
            
            for (i in fadeSteps downTo 0) {
                val volume = i / 100f
                controller?.volume = volume
                delay(stepDelay)
            }
            
            controller?.pause()
            controller?.volume = 1.0f // Reset for next play
            _sleepTimerActive.value = false
        }
    }

    fun stopSleepTimer() {
        sleepTimerJob?.cancel()
        controller?.volume = 1.0f
        _sleepTimerActive.value = false
    }

    override fun onCleared() {
        super.onCleared()
        controllerFuture?.let {
            MediaController.releaseFuture(it)
        }
    }
}

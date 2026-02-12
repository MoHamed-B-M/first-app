package com.mohamed.calmplayer.domain

import android.app.Application
import android.content.ComponentName
import androidx.lifecycle.AndroidViewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.mohamed.calmplayer.data.Song
import com.mohamed.calmplayer.service.MusicService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.google.common.util.concurrent.MoreExecutors

class MusicViewModel(application: Application) : AndroidViewModel(application) {

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying = _isPlaying.asStateFlow()

    private val _currentSong = MutableStateFlow<Song?>(null)
    val currentSong = _currentSong.asStateFlow()

    private var controller: MediaController? = null
    private var songList: List<Song> = emptyList()

    init {
        val sessionToken = SessionToken(application, ComponentName(application, MusicService::class.java))
        val controllerFuture = MediaController.Builder(application, sessionToken).buildAsync()
        controllerFuture.addListener({
            controller = controllerFuture.get()
            setupController()
        }, MoreExecutors.directExecutor())
    }

    private fun setupController() {
        controller?.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(playing: Boolean) {
                _isPlaying.value = playing
            }
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                val mediaId = mediaItem?.mediaId
                _currentSong.value = songList.find { it.id.toString() == mediaId }
            }
        })
    }

    fun playSong(song: Song, list: List<Song>) {
        songList = list
        val mediaItems = list.map { 
            MediaItem.Builder().setMediaId(it.id.toString()).setUri(it.uri).build() 
        }
        val index = list.indexOfFirst { it.id == song.id }
        
        controller?.apply {
            setMediaItems(mediaItems, index.coerceAtLeast(0), 0L)
            prepare()
            play()
        }
    }

    fun togglePlayPause() {
        if (controller?.isPlaying == true) controller?.pause() else controller?.play()
    }

    fun skipNext() = controller?.seekToNext()
    fun skipPrevious() = controller?.seekToPrevious()
    fun seekTo(pos: Long) = controller?.seekTo(pos)
}

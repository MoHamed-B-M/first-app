package com.mohamed.calmplayer.domain

import android.app.Application
import android.content.ComponentName
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.mohamed.calmplayer.data.MusicScanner
import com.mohamed.calmplayer.data.SettingsStore
import com.mohamed.calmplayer.data.Song
import com.mohamed.calmplayer.service.MusicService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.google.common.util.concurrent.MoreExecutors

class MusicViewModel(application: Application) : AndroidViewModel(application) {

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying = _isPlaying.asStateFlow()

    private val _currentSong = MutableStateFlow<Song?>(null)
    val currentSong = _currentSong.asStateFlow()

    private val _librarySongs = MutableStateFlow<List<Song>>(emptyList())
    val librarySongs = _librarySongs.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    val isScanning = _isScanning.asStateFlow()

    private var controller: MediaController? = null
    private var songList: List<Song> = emptyList()
    
    private val musicScanner = MusicScanner(application)
    private val settingsStore = SettingsStore(application)

    init {
        val sessionToken = SessionToken(application, ComponentName(application, MusicService::class.java))
        val controllerFuture = MediaController.Builder(application, sessionToken).buildAsync()
        controllerFuture.addListener({
            controller = controllerFuture.get()
            setupController()
        }, MoreExecutors.directExecutor())
        
        // Auto-load library when music folder is set
        viewModelScope.launch {
            settingsStore.musicFolderUriFlow.collect { uri ->
                uri?.let { scanAndLoadLibrary(it) }
            }
        }
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
    
    private fun scanAndLoadLibrary(folderUri: String) {
        viewModelScope.launch {
            try {
                _isScanning.value = true
                val songs = musicScanner.scanMusicFolder(folderUri)
                _librarySongs.value = songs
            } catch (e: Exception) {
                // Handle scanning error
                _librarySongs.value = emptyList()
            } finally {
                _isScanning.value = false
            }
        }
    }
    
    fun refreshLibrary() {
        viewModelScope.launch {
            val currentUri = settingsStore.musicFolderUriFlow.value
            currentUri?.let { scanAndLoadLibrary(it) }
        }
    }
}

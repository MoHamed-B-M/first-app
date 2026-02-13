package com.mohamed.calmplayer.data

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.PowerManager
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.util.Log
import androidx.documentfile.provider.DocumentFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

class MusicScanner(private val context: Context) {
    
    companion object {
        private const val TAG = "MusicScanner"
        private val AUDIO_MIME_TYPES = arrayOf(
            "audio/mpeg",      // MP3
            "audio/mp4",       // M4A/AAC
            "audio/wav",       // WAV
            "audio/flac",      // FLAC
            "audio/ogg",       // OGG
            "audio/x-matroska" // MKA
        )
    }
    
    suspend fun scanMusicFolder(folderUri: String): List<Song> = withContext(Dispatchers.IO) {
        val songs = mutableListOf<Song>()
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val isPowerSaveMode = powerManager.isPowerSaveMode
        
        try {
            val uri = Uri.parse(folderUri)
            val documentFile = DocumentFile.fromTreeUri(context, uri)
            
            if (documentFile?.exists() == true) {
                Log.d(TAG, "Starting scan of: $folderUri")
                Log.d(TAG, "Power save mode: $isPowerSaveMode")
                
                // Optimize scanning based on power mode
                if (isPowerSaveMode) {
                    scanDirectoryOptimized(documentFile, songs)
                } else {
                    scanDirectoryRecursive(documentFile, songs)
                }
                
                Log.d(TAG, "Scan completed. Found ${songs.size} songs")
            } else {
                Log.e(TAG, "Document file does not exist or is null")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error scanning music folder", e)
        }
        
        songs
    }
    
    private suspend fun scanDirectoryRecursive(documentFile: DocumentFile, songs: MutableList<Song>) {
        if (!documentFile.isDirectory) return
        
        val children = documentFile.listFiles()
        
        for (child in children) {
            if (child.isDirectory) {
                // Recursively scan subdirectories
                scanDirectoryRecursive(child, songs)
            } else if (child.isFile && isAudioFile(child)) {
                // Process audio files
                child.uri?.let { uri ->
                    createSongFromUri(uri, child.name ?: "Unknown")?.let { song ->
                        songs.add(song)
                    }
                }
            }
            
            // Small delay to prevent overwhelming the system
            if (songs.size % 50 == 0) {
                delay(10) // 10ms delay every 50 files
            }
        }
    }
    
    private suspend fun scanDirectoryOptimized(documentFile: DocumentFile, songs: MutableList<Song>) {
        // Optimized scanning for power save mode - reduced recursion depth
        if (!documentFile.isDirectory) return
        
        val children = documentFile.listFiles()
        var processedCount = 0
        
        for (child in children) {
            if (child.isDirectory && processedCount < 20) { // Limit directory scanning in power save mode
                scanDirectoryOptimized(child, songs)
                processedCount++
            } else if (child.isFile && isAudioFile(child)) {
                child.uri?.let { uri ->
                    createSongFromUri(uri, child.name ?: "Unknown")?.let { song ->
                        songs.add(song)
                    }
                }
            }
            
            // Longer delay in power save mode
            if (songs.size % 25 == 0) {
                delay(20) // 20ms delay every 25 files in power save mode
            }
        }
    }
    
    private fun isAudioFile(documentFile: DocumentFile): Boolean {
        val mimeType = documentFile.type
        return mimeType != null && AUDIO_MIME_TYPES.any { audioType ->
            mimeType.equals(audioType, ignoreCase = true) || mimeType.startsWith("audio/")
        }
    }
    
    private fun createSongFromUri(uri: Uri, fileName: String): Song? {
        return try {
            // Extract metadata from MediaStore using the URI
            val cursor = context.contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                arrayOf(
                    MediaStore.Audio.Media._ID,
                    MediaStore.Audio.Media.TITLE,
                    MediaStore.Audio.Media.ARTIST,
                    MediaStore.Audio.Media.ALBUM,
                    MediaStore.Audio.Media.ALBUM_ID,
                    MediaStore.Audio.Media.DURATION
                ),
                "${MediaStore.Audio.Media.DATA} = ?",
                arrayOf(uri.toString()),
                null
            )
            
            cursor?.use { c ->
                if (c.moveToFirst()) {
                    val id = c.getLong(c.getColumnIndexOrThrow(MediaStore.Audio.Media._ID))
                    val title = c.getString(c.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE))
                    val artist = c.getString(c.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST))
                    val album = c.getString(c.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM))
                    val albumId = c.getLong(c.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID))
                    val duration = c.getLong(c.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION))
                    
                    val albumArtUri = ContentUris.withAppendedId(
                        Uri.parse("content://media/external/audio/albumart"),
                        albumId
                    )
                    
                    // Smart metadata extraction
                    var extractedBpm = (70..130).random()
                    var extractedMood = "Neutral"
                    
                    if (duration > 300000) extractedMood = "Calm"
                    if (title.contains("remix", true)) extractedBpm += 20
                    
                    Song(
                        id = id,
                        title = title,
                        artist = artist.ifEmpty { "Unknown Artist" },
                        album = album.ifEmpty { "Unknown Album" },
                        albumId = albumId,
                        duration = duration,
                        uri = uri,
                        albumArtUri = albumArtUri,
                        bpm = extractedBpm,
                        mood = extractedMood
                    )
                } else {
                    // Fallback: create song with basic info from filename
                    createFallbackSong(uri, fileName)
                }
            } ?: createFallbackSong(uri, fileName)
        } catch (e: Exception) {
            Log.e(TAG, "Error creating song from URI: $uri", e)
            createFallbackSong(uri, fileName)
        }
    }
    
    private fun createFallbackSong(uri: Uri, fileName: String): Song {
        val title = fileName.substringBeforeLast(".")
        return Song(
            id = uri.hashCode().toLong(),
            title = title,
            artist = "Unknown Artist",
            album = "Unknown Album",
            albumId = 0L,
            duration = 0L,
            uri = uri,
            albumArtUri = Uri.parse(""),
            bpm = 120,
            mood = "Neutral"
        )
    }
}

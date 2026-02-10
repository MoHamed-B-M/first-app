package com.mohamed.calmplayer.data

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.MediaStore

data class Song(
    val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val albumId: Long,
    val duration: Long,
    val uri: Uri,
    val albumArtUri: Uri
)

data class Album(
    val id: Long,
    val name: String,
    val artist: String,
    val albumArtUri: Uri,
    val numberOfSongs: Int
)

data class Artist(
    val id: Long,
    val name: String,
    val numberOfTracks: Int,
    val numberOfAlbums: Int
)

class MediaLibraryHelper(private val context: Context) {

    fun getAllSongs(): List<Song> {
        val songList = mutableListOf<Song>()
        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        }

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.DURATION
        )

        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
        val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"

        try {
            val cursor: Cursor? = context.contentResolver.query(
                collection,
                projection,
                selection,
                null,
                sortOrder
            )

            cursor?.use {
                val idColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val titleColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val artistColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val albumColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
                val albumIdColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
                val durationColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)

                while (it.moveToNext()) {
                    val id = it.getLong(idColumn)
                    val title = it.getString(titleColumn)
                    val artist = it.getString(artistColumn)
                    val album = it.getString(albumColumn)
                    val albumId = it.getLong(albumIdColumn)
                    val duration = it.getLong(durationColumn)

                    val contentUri = ContentUris.withAppendedId(collection, id)
                    val albumArtUri = ContentUris.withAppendedId(
                        Uri.parse("content://media/external/audio/albumart"),
                        albumId
                    )

                    songList.add(
                        Song(id, title, artist, album, albumId, duration, contentUri, albumArtUri)
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return songList
    }

    fun getAllAlbums(): List<Album> {
        // Implementation for albums query could be added here similar to songs
        // simplified mapping from songs for now or distinct query
        val songs = getAllSongs()
        return songs.groupBy { it.albumId }.map { (albumId, songs) ->
            Album(
                id = albumId,
                name = songs.first().album,
                artist = songs.first().artist,
                albumArtUri = songs.first().albumArtUri,
                numberOfSongs = songs.size
            )
        }
    }
}

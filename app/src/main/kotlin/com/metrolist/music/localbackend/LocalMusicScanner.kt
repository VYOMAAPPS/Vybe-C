/*
 * VYBE local-files backend (see LocalBackend.kt).
 */

package com.metrolist.music.localbackend

import android.content.ContentUris
import android.content.Context
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class LocalTrack(
    val mediaStoreId: Long,
    val title: String,
    val artist: String,
    val album: String?,
    val albumMediaStoreId: Long?,
    val durationSec: Int,
    val year: Int?,
    val dateAddedSec: Long,
    val dateModifiedSec: Long,
) {
    val id: String get() = LocalBackend.idFor(mediaStoreId)

    /** Album art served by MediaStore; Coil loads content:// URIs without extra setup. */
    val artworkUri: String?
        get() = albumMediaStoreId?.let {
            ContentUris
                .withAppendedId(android.net.Uri.parse("content://media/external/audio/albumart"), it)
                .toString()
        }
}

object LocalMusicScanner {
    private const val MIN_DURATION_MS = 10_000L

    suspend fun scan(context: Context): List<LocalTrack> =
        withContext(Dispatchers.IO) {
            if (!LocalBackend.hasAudioPermission(context)) return@withContext emptyList()

            val projection =
                arrayOf(
                    MediaStore.Audio.Media._ID,
                    MediaStore.Audio.Media.TITLE,
                    MediaStore.Audio.Media.ARTIST,
                    MediaStore.Audio.Media.ALBUM,
                    MediaStore.Audio.Media.ALBUM_ID,
                    MediaStore.Audio.Media.DURATION,
                    MediaStore.Audio.Media.YEAR,
                    MediaStore.Audio.Media.DATE_ADDED,
                    MediaStore.Audio.Media.DATE_MODIFIED,
                )
            val selection =
                "${MediaStore.Audio.Media.IS_MUSIC} != 0 AND ${MediaStore.Audio.Media.DURATION} >= $MIN_DURATION_MS"

            val tracks = mutableListOf<LocalTrack>()
            runCatching {
                context.contentResolver
                    .query(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        projection,
                        selection,
                        null,
                        "${MediaStore.Audio.Media.DATE_ADDED} DESC",
                    )?.use { cursor ->
                        val idCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                        val titleCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                        val artistCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                        val albumCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
                        val albumIdCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
                        val durationCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                        val yearCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.YEAR)
                        val addedCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)
                        val modifiedCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_MODIFIED)

                        while (cursor.moveToNext()) {
                            val rawArtist = cursor.getString(artistCol)?.trim().orEmpty()
                            val rawAlbum = cursor.getString(albumCol)?.trim()
                            tracks +=
                                LocalTrack(
                                    mediaStoreId = cursor.getLong(idCol),
                                    title = cursor.getString(titleCol)?.trim().orEmpty().ifBlank { "Unknown" },
                                    artist = rawArtist.takeUnless { it.isBlank() || it == "<unknown>" } ?: "Unknown artist",
                                    album = rawAlbum?.takeUnless { it.isBlank() || it == "<unknown>" },
                                    albumMediaStoreId = cursor.getLong(albumIdCol).takeIf { it > 0 },
                                    durationSec = (cursor.getLong(durationCol) / 1000L).toInt(),
                                    year = cursor.getInt(yearCol).takeIf { it > 0 },
                                    dateAddedSec = cursor.getLong(addedCol),
                                    dateModifiedSec = cursor.getLong(modifiedCol),
                                )
                        }
                    }
            }
            tracks
        }
}

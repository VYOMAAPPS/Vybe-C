/*
 * VYBE local-files backend (see LocalBackend.kt).
 */

package com.metrolist.music.localbackend

import android.content.Context
import com.metrolist.music.db.MusicDatabase
import com.metrolist.music.db.entities.AlbumEntity
import com.metrolist.music.db.entities.ArtistEntity
import com.metrolist.music.db.entities.SongAlbumMap
import com.metrolist.music.db.entities.SongArtistMap
import com.metrolist.music.db.entities.SongEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * Mirrors the phone's audio files into the app database so every existing screen
 * (Home, Search, Library, Player, queue, stats...) works unchanged.
 */
object LocalLibrarySync {
    private fun toLocalDateTime(epochSec: Long): LocalDateTime =
        LocalDateTime.ofInstant(Instant.ofEpochSecond(epochSec), ZoneId.systemDefault())

    private fun artistId(name: String): String =
        // "LA" prefix = local artist in the existing code base (no online lookups).
        "LA" + "local_" + Integer.toHexString(name.lowercase().hashCode())

    private fun albumId(track: LocalTrack): String =
        "LBlocal_" + (track.albumMediaStoreId ?: Integer.toHexString((track.album ?: "").lowercase().hashCode()))

    suspend fun sync(
        context: Context,
        database: MusicDatabase,
    ) = withContext(Dispatchers.IO) {
        if (!LocalBackend.hasAudioPermission(context)) return@withContext
        val tracks = LocalMusicScanner.scan(context)
        val knownIds = tracks.map { it.id }.toSet()

        // Songs that were deleted from the phone disappear from the library.
        val stale =
            database
                .allSongs()
                .first()
                .map { it.song }
                .filter { LocalBackend.isLocalId(it.id) && it.id !in knownIds }

        val albumStats = tracks.filter { it.album != null }.groupBy { albumId(it) }

        database.transaction {
            stale.forEach { delete(it) }

            tracks.forEach { track ->
                val songId = track.id
                val aId = artistId(track.artist)
                val alId = track.album?.let { albumId(track) }

                insert(
                    ArtistEntity(
                        id = aId,
                        name = track.artist,
                        isLocal = true,
                    ),
                )
                if (alId != null) {
                    val group = albumStats[alId].orEmpty()
                    insert(
                        AlbumEntity(
                            id = alId,
                            title = track.album ?: "",
                            year = track.year,
                            thumbnailUrl = track.artworkUri,
                            songCount = group.size,
                            duration = group.sumOf { it.durationSec },
                            inLibrary = LocalDateTime.now(),
                            isLocal = true,
                        ),
                    )
                }
                insert(
                    SongEntity(
                        id = songId,
                        title = track.title,
                        duration = track.durationSec,
                        thumbnailUrl = track.artworkUri,
                        albumId = alId,
                        albumName = track.album,
                        year = track.year,
                        dateModified = toLocalDateTime(track.dateModifiedSec),
                        inLibrary = toLocalDateTime(track.dateAddedSec),
                        isLocal = true,
                    ),
                )
                insert(SongArtistMap(songId = songId, artistId = aId, position = 0))
                if (alId != null) {
                    insert(SongAlbumMap(songId = songId, albumId = alId, index = 0))
                }
            }
        }
    }
}

package com.metrolist.innertube

import com.metrolist.innertube.models.MediaInfo
import com.metrolist.innertube.models.YouTubeClient
import com.metrolist.innertube.models.YouTubeLocale
import io.ktor.client.statement.HttpResponse
import java.io.InputStream
import java.net.Proxy

/** Thrown by every online call while the app runs on the local-files backend. */
class BackendDisabledException(
    operation: String,
) : UnsupportedOperationException("Online backend is disabled (local files mode): $operation")

/**
 * Offline stand-in for the old online client.
 *
 * Every request fails fast with [BackendDisabledException]; callers already wrap these in
 * runCatching / Result, so the UI simply sees "no online data". To add your own API later,
 * implement the requests here (or replace the YouTube object) and flip LOCAL_BACKEND_ENABLED
 * in app/build.gradle.kts.
 */
class InnerTube(
    @Suppress("UNUSED_PARAMETER") client: Any? = null,
) {
    class ExtractionTransport internal constructor(
        val generation: Long,
    )

    var locale: YouTubeLocale = YouTubeLocale(gl = "US", hl = "en")
    var visitorData: String? = null
    var dataSyncId: String? = null
    var authUser: String = "0"
    var cookie: String? = null
    var proxy: Proxy? = null
    var proxyAuth: String? = null
    var useLoginForBrowse: Boolean = false

    fun extractionTransport(): ExtractionTransport = ExtractionTransport(0L)

    private fun off(operation: String): Nothing = throw BackendDisabledException(operation)

    suspend fun search(
        client: YouTubeClient,
        query: String? = null,
        params: String? = null,
        continuation: String? = null,
        setLogin: Boolean = false,
    ): HttpResponse = off("search")

    suspend fun player(
        client: YouTubeClient,
        videoId: String,
        playlistId: String?,
        signatureTimestamp: Int?,
        poToken: String? = null,
    ): HttpResponse = off("player")

    suspend fun registerPlayback(
        videoId: String,
        cpn: String,
        isHistoryEnabled: () -> Boolean = { true },
    ): Boolean = false

    suspend fun browse(
        client: YouTubeClient,
        browseId: String? = null,
        params: String? = null,
        continuation: String? = null,
        setLogin: Boolean = false,
    ): HttpResponse = off("browse")

    suspend fun next(
        client: YouTubeClient,
        videoId: String?,
        playlistId: String?,
        playlistSetVideoId: String?,
        index: Int?,
        params: String?,
        continuation: String? = null,
    ): HttpResponse = off("next")

    suspend fun feedback(
        client: YouTubeClient,
        tokens: List<String>,
    ): HttpResponse = off("feedback")

    suspend fun getSearchSuggestions(
        client: YouTubeClient,
        input: String,
    ): HttpResponse = off("getSearchSuggestions")

    suspend fun getQueue(
        client: YouTubeClient,
        videoIds: List<String>?,
        playlistId: String?,
    ): HttpResponse = off("getQueue")

    suspend fun getTranscript(
        client: YouTubeClient,
        videoId: String,
    ): HttpResponse = off("getTranscript")

    suspend fun fetchFreshVisitorData(): String? = null

    suspend fun accountMenu(client: YouTubeClient): HttpResponse = off("accountMenu")

    suspend fun accountsList(): HttpResponse = off("accountsList")

    suspend fun likeVideo(client: YouTubeClient, videoId: String): HttpResponse = off("likeVideo")

    suspend fun unlikeVideo(client: YouTubeClient, videoId: String): HttpResponse = off("unlikeVideo")

    suspend fun subscribeChannel(client: YouTubeClient, channelId: String, params: String? = null): HttpResponse = off("subscribeChannel")

    suspend fun unsubscribeChannel(client: YouTubeClient, channelId: String, params: String? = null): HttpResponse = off("unsubscribeChannel")

    suspend fun likePlaylist(client: YouTubeClient, playlistId: String): HttpResponse = off("likePlaylist")

    suspend fun unlikePlaylist(client: YouTubeClient, playlistId: String): HttpResponse = off("unlikePlaylist")

    suspend fun addToPlaylist(client: YouTubeClient, playlistId: String, videoId: String): HttpResponse = off("addToPlaylist")

    suspend fun addPlaylistToPlaylist(client: YouTubeClient, playlistId: String, addedPlaylistId: String): HttpResponse = off("addPlaylistToPlaylist")

    suspend fun removeFromPlaylist(client: YouTubeClient, playlistId: String, videoId: String, setVideoId: String): HttpResponse = off("removeFromPlaylist")

    suspend fun moveSongPlaylist(client: YouTubeClient, playlistId: String, setVideoId: String, successorSetVideoId: String?): HttpResponse = off("moveSongPlaylist")

    suspend fun createPlaylist(client: YouTubeClient, title: String): HttpResponse = off("createPlaylist")

    suspend fun renamePlaylist(client: YouTubeClient, playlistId: String, name: String): HttpResponse = off("renamePlaylist")

    suspend fun setPlaylistThumbnail(client: YouTubeClient, playlistId: String, image: ByteArray): HttpResponse = off("setPlaylistThumbnail")

    suspend fun removePlaylistThumbnail(client: YouTubeClient, playlistId: String): HttpResponse = off("removePlaylistThumbnail")

    suspend fun deletePlaylist(client: YouTubeClient, playlistId: String): HttpResponse = off("deletePlaylist")

    suspend fun uploadSong(
        filename: String,
        contentLength: Long,
        content: () -> InputStream,
    ): HttpResponse = off("uploadSong")

    suspend fun deletePrivatelyOwnedEntity(entityId: String): HttpResponse = off("deletePrivatelyOwnedEntity")

    suspend fun getMediaInfo(videoId: String): Result<MediaInfo> =
        Result.failure(BackendDisabledException("getMediaInfo"))
}

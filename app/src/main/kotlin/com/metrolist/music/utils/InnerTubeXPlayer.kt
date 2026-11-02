package com.metrolist.music.utils

import android.content.Context
import android.net.ConnectivityManager
import com.metrolist.innertube.models.response.PlayerResponse
import com.metrolist.music.constants.AudioQuality

/** Hints the (removed) online stream resolver used. Kept so existing call sites compile. */
data class ContentHints(
    val isExplicit: Boolean? = null,
    val isUploaded: Boolean? = null,
)

fun generateClientPlaybackNonce(): String = java.util.UUID.randomUUID().toString().replace("-", "").take(16)

/**
 * Offline replacement for the old online stream resolver.
 *
 * Local songs (ids starting with "local_") never reach this object: MusicService hands their
 * content:// URI straight to the player. Anything else fails here with a clear message.
 * When you plug in your own API, resolve the stream URL for [videoId] here.
 */
object InnerTubeXPlayer {
    @Synchronized
    fun initialize(@Suppress("UNUSED_PARAMETER") context: Context) = Unit

    suspend fun prewarm() = Unit

    @Suppress("UNUSED_PARAMETER")
    suspend fun playerResponseForPlayback(
        videoId: String,
        playlistId: String? = null,
        audioQuality: AudioQuality,
        connectivityManager: ConnectivityManager,
        contentHints: ContentHints = ContentHints(),
        allowBoundedRange: Boolean = true,
    ): Result<PlaybackData> =
        Result.failure(
            UnsupportedOperationException("Online playback is disabled (local files mode): $videoId"),
        )

    @Suppress("UNUSED_PARAMETER")
    internal fun markStreamClientFailed(
        videoId: String,
        clientName: String,
        nowMs: Long = System.currentTimeMillis(),
    ) = Unit

    fun clearStreamClientFailures() = Unit

    suspend fun refreshAfterStreamRejection(): Boolean = false

    class PlaybackData(
        val audioConfig: PlayerResponse.PlayerConfig.AudioConfig?,
        val videoDetails: PlayerResponse.VideoDetails?,
        val playbackTracking: PlayerResponse.PlaybackTracking?,
        val format: PlayerResponse.StreamingData.Format,
        val streamUrl: String,
        val streamExpiresInSeconds: Int,
        val streamClient: String,
        val streamHeaders: Map<String, String>,
        val requireBoundedRange: Boolean,
        val rangeChunkSizeBytes: Long,
        val useRangeChunks: Boolean,
    )
}

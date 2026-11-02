package com.metrolist.innertube.models

/**
 * Minimal local stand-in for the removed external client descriptor.
 * Only the names are kept so the existing (now offline) call sites still compile.
 */
class YouTubeClient private constructor(
    val clientName: String,
) {
    companion object {
        val WEB = YouTubeClient("WEB")
        val WEB_REMIX = YouTubeClient("WEB_REMIX")
    }
}

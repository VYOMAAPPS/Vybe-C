/*
 * VYBE local-files backend (see LocalBackend.kt).
 */

package com.metrolist.music.localbackend

import android.content.Context
import android.net.Uri
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DataSpec
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.TransferListener

/**
 * Sends content:// and file:// reads straight to the phone's storage (no cache copy),
 * and everything else to the normal online data source.
 */
@UnstableApi
class LocalAwareDataSourceFactory(
    private val context: Context,
    private val online: DataSource.Factory,
) : DataSource.Factory {
    override fun createDataSource(): DataSource =
        LocalAwareDataSource(
            online = online.createDataSource(),
            local = DefaultDataSource.Factory(context).createDataSource(),
        )
}

@UnstableApi
private class LocalAwareDataSource(
    private val online: DataSource,
    private val local: DataSource,
) : DataSource {
    private var active: DataSource? = null
    private val listeners = mutableListOf<TransferListener>()

    override fun addTransferListener(transferListener: TransferListener) {
        listeners += transferListener
        online.addTransferListener(transferListener)
        local.addTransferListener(transferListener)
    }

    override fun open(dataSpec: DataSpec): Long {
        val scheme = dataSpec.uri.scheme
        val target = if (scheme == "content" || scheme == "file") local else online
        active = target
        return target.open(dataSpec)
    }

    override fun read(
        buffer: ByteArray,
        offset: Int,
        length: Int,
    ): Int = checkNotNull(active).read(buffer, offset, length)

    override fun getUri(): Uri? = active?.uri

    override fun getResponseHeaders(): Map<String, List<String>> = active?.responseHeaders ?: emptyMap()

    override fun close() {
        try {
            active?.close()
        } finally {
            active = null
        }
    }
}

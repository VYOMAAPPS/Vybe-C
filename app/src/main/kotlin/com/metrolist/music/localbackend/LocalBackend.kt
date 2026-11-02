/*
 * VYBE local-files backend.
 *
 * Everything that makes VYBE play music from the phone's storage lives in this package.
 * To switch to your own API later:
 *   1. set LOCAL_BACKEND_ENABLED to false in app/build.gradle.kts,
 *   2. implement your API in innertube/.../InnerTube.kt + utils/InnerTubeXPlayer.kt,
 *   3. delete this package and the 3 small hooks marked "LOCAL_BACKEND" (grep for it).
 */

package com.metrolist.music.localbackend

import android.Manifest
import android.content.ContentUris
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.core.content.ContextCompat
import com.metrolist.music.BuildConfig

object LocalBackend {
    const val ID_PREFIX = "local_"

    val enabled: Boolean get() = BuildConfig.LOCAL_BACKEND_ENABLED

    fun isLocalId(id: String?): Boolean = id != null && id.startsWith(ID_PREFIX)

    fun idFor(mediaStoreId: Long): String = "$ID_PREFIX$mediaStoreId"

    fun uriFor(localId: String): Uri =
        ContentUris.withAppendedId(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            localId.removePrefix(ID_PREFIX).toLong(),
        )

    fun audioPermission(): String =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

    fun hasAudioPermission(context: Context): Boolean =
        ContextCompat.checkSelfPermission(context, audioPermission()) == PackageManager.PERMISSION_GRANTED
}

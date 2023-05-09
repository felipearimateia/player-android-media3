package br.com.dicasdoari.player

import android.net.Uri

data class MediaAudio(
    val name: String,
    val uri: String,
    val position: Long? = null,
    val artworkUri:  Uri? = null
)

fun MediaAudio.getUri(): String = uri.replace(" ", "%20")

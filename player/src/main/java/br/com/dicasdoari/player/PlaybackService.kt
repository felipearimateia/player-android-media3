package br.com.dicasdoari.player


import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture

class PlaybackService: MediaSessionService(), MediaSession.Callback {

    private var mediaSession: MediaSession? = null
    private lateinit var player: ExoPlayer

    private val audioAttributes = AudioAttributes.Builder()
        .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
        .setUsage(C.USAGE_MEDIA)
        .build()

    override fun onCreate() {
        super.onCreate()
        initExoPlayer()
        mediaSession = MediaSession.Builder(this, player)
            .setCallback(this)
            .build()
    }

    override fun onDestroy() {
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        super.onDestroy()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo) = mediaSession

    override fun onAddMediaItems(
        mediaSession: MediaSession,
        controller: MediaSession.ControllerInfo,
        mediaItems: MutableList<MediaItem>
    ): ListenableFuture<MutableList<MediaItem>> {
        val updatedMediaItems = mediaItems.map {
            it.buildUpon().setUri(it.mediaId).build()
        }.toMutableList()
        return Futures.immediateFuture(updatedMediaItems)
    }


    private fun initExoPlayer() {
        player = ExoPlayer.Builder(this)
            .setAudioAttributes(audioAttributes, true)
            .setHandleAudioBecomingNoisy(true)
            .build()
    }
}
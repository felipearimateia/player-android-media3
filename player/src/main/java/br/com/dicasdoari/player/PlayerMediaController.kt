package br.com.dicasdoari.player

import android.content.ComponentName
import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors

class PlayerMediaController(context: Context): Playback {

    private var controllerFuture: ListenableFuture<MediaController>
    private val controller: MediaController?
        get() = if (controllerFuture.isDone) controllerFuture.get() else null

    var callback: ((event: PlayerEvent, currentTime: Long, totalDuration: Long, isPlaying: Boolean) -> Unit)? = null
    private var startPosition: Long? = null

    init {
        val sessionToken = SessionToken(context, ComponentName(context, PlaybackService::class.java))
        controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
        controllerFuture.addListener({configureController()}, MoreExecutors.directExecutor())
    }

    private fun configureController(){
        controller?.addListener( object: Player.Listener {
            override fun onEvents(player: Player, events: Player.Events) {
                super.onEvents(player, events)
                val currentTime = player.currentPosition.coerceAtLeast(0L)
                val totalDuration = player.duration.coerceAtLeast(0L)
                val isPlaying = player.isPlaying
                val event = mapPlaybackState(player.playbackState, isPlaying)
                callback?.invoke(event, currentTime, totalDuration, isPlaying)
                if (player.playbackState == Player.STATE_READY && startPosition != null) {
                    player.seekTo(startPosition!!)
                    startPosition = null
                }
            }
        } )
    }

    private fun mapPlaybackState(playbackState: Int, isPlaying: Boolean): PlayerEvent {
        return when (playbackState) {
            Player.STATE_IDLE -> PlayerEvent.STOPPED
            Player.STATE_BUFFERING -> PlayerEvent.LOADING
            Player.STATE_ENDED -> PlayerEvent.STOPPED
            else -> if (isPlaying) PlayerEvent.PLAYING else PlayerEvent.PAUSED
        }
    }

    override fun play(mediaAudio: MediaAudio) {
        val source = mediaAudio.getUri()
        val mediaItem = MediaItem.Builder()
            .setMediaId(source)
            .setUri(source)
            .setMediaMetadata(buildMediaMetadata(mediaAudio))
            .build()

        controller?.run {
            startPosition = mediaAudio.position
            setMediaItem(mediaItem)
            playWhenReady = true
            prepare()
        }
    }

    private fun buildMediaMetadata(mediaAudio: MediaAudio): MediaMetadata {
        val builder = MediaMetadata.Builder()
            .setTitle(mediaAudio.name)

        mediaAudio.artworkUri?.let {
            builder.setArtworkUri(it)
        }
        return builder.build()
    }

    override fun pause() {
        controller?.pause()
    }

    override fun resume() {
        controller?.play()
    }

    override fun stop() {
        controller?.stop()
    }

    override fun seekTo(position: Long) {
        controller?.seekTo(position)
    }

    override fun speed(speed: Float) {
        controller?.setPlaybackSpeed(speed)
    }

    override fun getCurrentPosition(): Long = controller?.currentPosition ?: 0L

    override fun destroy() {
        MediaController.releaseFuture(controllerFuture)
        callback = null
    }
}
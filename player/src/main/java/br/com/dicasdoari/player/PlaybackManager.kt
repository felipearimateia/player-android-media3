package br.com.dicasdoari.player

import android.util.Log
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player

class PlaybackManager: Player.Listener {

    private val LOG_TAG: String = "PlaybackManager"

    override fun onPlayerError(error: PlaybackException) {
        Log.e(LOG_TAG, "onPlayerError: ${error.message}")
    }

    override fun onPlaybackStateChanged(playbackState: Int) {
        Log.d(LOG_TAG, "onPlaybackStateChanged: $playbackState")
    }

    override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
        Log.d(LOG_TAG, "onPlayWhenReadyChanged: $playWhenReady")
    }

    override fun onIsPlayingChanged(isPlaying: Boolean) {
        Log.d(LOG_TAG, "onIsPlayingChanged: $isPlaying")
    }
}
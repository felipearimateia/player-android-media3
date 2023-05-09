package br.com.dicasdoari.player

interface Playback {
    fun play(mediaAudio: MediaAudio)
    fun pause()
    fun resume()
    fun stop()
    fun seekTo(position: Long)
    fun speed(speed: Float)
    fun getCurrentPosition(): Long
    fun destroy()
}
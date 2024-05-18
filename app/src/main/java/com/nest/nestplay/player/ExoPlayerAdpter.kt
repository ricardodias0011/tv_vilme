package com.nest.nestplay.player

import android.content.Context
import androidx.leanback.media.PlayerAdapter
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player
import com.nest.nestplay.model.MovieModel
import com.nest.nestplay.utils.Common

class ExoPlayerAdapter(
    private val context: Context,
    private val exoPlayer: ExoPlayer
) : PlayerAdapter() {

    private var onPreparedListener: (() -> Unit)? = null
    private var onErrorListener: ((error: Int, extra: Int) -> Boolean)? = null

    fun setOnPreparedListener(listener: () -> Unit) {
        onPreparedListener = listener
    }

    fun setOnErrorListener(listener: (error: Int, extra: Int) -> Boolean) {
        onErrorListener = listener
    }

    override fun play() {
        exoPlayer.play()
    }

    override fun pause() {
        exoPlayer.pause()
    }

    override fun isPlaying(): Boolean {
        return exoPlayer.isPlaying
    }

    override fun getDuration(): Long {
        return exoPlayer.duration
    }

    override fun getCurrentPosition(): Long {
        return exoPlayer.currentPosition
    }

    override fun seekTo(position: Long) {
        exoPlayer.seekTo(position)
    }

    override fun fastForward() {
        seekTo(currentPosition + 20000)
    }

    override fun rewind() {
        seekTo(currentPosition - 20000)
    }

    override fun next() {
        seekTo(currentPosition + 30000)
    }

    override fun previous() {
        seekTo(currentPosition - 30000)
    }

    fun highQuality() {
        Common.changeQuality(context)
    }

    fun subtitle(currentMovie: MovieModel) {
        var subtitles = listOf<String>()
        if (currentMovie.subtitles != null) {
            subtitles = currentMovie.subtitles!!
        }
        Common.changeSubtitle(context, subtitles)
    }

//    override fun getSupportedActions(): Long {
//        onPreparedListener?.invoke()
//        return (ACTION_SKIP_TO_PREVIOUS xor
//                ACTION_REWIND xor
//                ACTION_PLAY_PAUSE xor
//                ACTION_FAST_FORWARD xor
//                ACTION_SKIP_TO_NEXT
//                ).toLong()
//    }

    fun stop() {
        pause()
    }

    init {
        exoPlayer.addListener(object : Player.Listener {
            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                if (playbackState == Player.STATE_READY) {
                    onPreparedListener?.invoke()
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                onErrorListener?.invoke(error.errorCode, error.errorCode)
            }
        })
    }
}

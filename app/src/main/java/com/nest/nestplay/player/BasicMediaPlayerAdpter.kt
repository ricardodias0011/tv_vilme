package com.nest.nestplay.player

import android.content.Context
import androidx.leanback.media.MediaPlayerAdapter
import androidx.leanback.media.PlaybackControlGlue.ACTION_FAST_FORWARD
import androidx.leanback.media.PlaybackControlGlue.ACTION_PLAY_PAUSE
import androidx.leanback.media.PlaybackControlGlue.ACTION_REWIND
import androidx.leanback.media.PlaybackControlGlue.ACTION_SKIP_TO_NEXT
import androidx.leanback.media.PlaybackControlGlue.ACTION_SKIP_TO_PREVIOUS
import com.nest.nestplay.model.MovieModel
import com.nest.nestplay.utils.Common

class BasicMediaPlayerAdpter(context: Context): MediaPlayerAdapter(context) {
    val playlist = ArrayList<String>()
    val playlistPosition = 0
    private var onPreparedListener: (() -> Unit)? = null
    private var onErrorListener: ((error: Int, extra: Int) -> Boolean)? = null
    fun setOnPreparedListener(listener: () -> Unit) {
        onPreparedListener = listener
    }

    fun setOnErrorListener(listener: (error: Int, extra: Int) -> Boolean) {
        onErrorListener = listener
    }
    override fun onError(what: Int, extra: Int): Boolean {
        return onErrorListener?.invoke(what, extra) ?: super.onError(what, extra)
    }

    override fun next(){
        super.next()

        val newPosition = mediaPlayer?.currentPosition?.plus(30000) ?: return
        mediaPlayer?.seekTo(newPosition)
    }

    override fun previous(){
        super.previous()
    }

    override fun fastForward(){
        seekTo(currentPosition + 20_000)
    }

    fun highQuality(context: Context){
        Common.changeQuality(context)
        println(mediaPlayer)
    }


    fun subtitle(context: Context, currentMovie: MovieModel){
        var subtitles = listOf<String>()
        if(currentMovie.subtitles != null){
            subtitles = currentMovie.subtitles!!
        }
        Common.changeSubtitle(context, subtitles)
    }

    override fun rewind(){
        seekTo(currentPosition - 20_000)
    }

    override fun getSupportedActions(): Long {
        onPreparedListener?.invoke()
        return (ACTION_SKIP_TO_PREVIOUS xor
                ACTION_REWIND xor
                ACTION_PLAY_PAUSE xor
                ACTION_FAST_FORWARD xor
                ACTION_SKIP_TO_NEXT
                ).toLong()
    }

    fun stop() {
        pause()
    }
}
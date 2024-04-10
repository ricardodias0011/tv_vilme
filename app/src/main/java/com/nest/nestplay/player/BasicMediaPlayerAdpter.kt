package com.nest.nestplay.player

import android.content.Context
import androidx.leanback.media.MediaPlayerAdapter
import androidx.leanback.media.PlaybackControlGlue.ACTION_FAST_FORWARD
import androidx.leanback.media.PlaybackControlGlue.ACTION_PLAY_PAUSE
import androidx.leanback.media.PlaybackControlGlue.ACTION_REWIND
import androidx.leanback.media.PlaybackControlGlue.ACTION_SKIP_TO_NEXT
import androidx.leanback.media.PlaybackControlGlue.ACTION_SKIP_TO_PREVIOUS

class BasicMediaPlayerAdpter(context: Context): MediaPlayerAdapter(context) {
    val playlist = ArrayList<String>()
    val playlistPosition = 0

    override fun next(){
        super.next()
    }

    override fun previous(){
        super.previous()
    }
    override fun fastForward(){
        seekTo(currentPosition + 10_000)
    }
    override fun rewind(){
        seekTo(currentPosition - 10_000)
    }

    override fun getSupportedActions(): Long {
        return (ACTION_SKIP_TO_PREVIOUS xor
                ACTION_REWIND xor
                ACTION_PLAY_PAUSE xor
                ACTION_FAST_FORWARD xor
                ACTION_SKIP_TO_NEXT).toLong()
    }

    fun loadingMovies(){

    }
}
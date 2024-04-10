package com.nest.nestplay.player

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.ui.StyledPlayerView
import com.nest.nestplay.R

class VideoPlayActivity: AppCompatActivity() {

    private var player: ExoPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_play)
        val videoURL = intent.getIntExtra("url_link", 0)
        val playerView: StyledPlayerView = findViewById(R.id.playerviewDetail)
        player = ExoPlayer.Builder(this).build()
        playerView.player = player
        val mediaItem: MediaItem = MediaItem.fromUri(videoURL as String)
        player?.setMediaItem(mediaItem)
        player?.prepare()
        player?.playWhenReady = true
    }

    override fun onStop() {
        super.onStop()
        player?.playWhenReady = false
        player?.release()
        player = null
    }

}
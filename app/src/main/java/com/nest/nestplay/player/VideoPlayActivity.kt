package com.nest.nestplay.player

import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.ui.PlayerView
import com.nest.nestplay.R

class VideoPlayActivity: AppCompatActivity() {
    private lateinit var playerView: PlayerView
    private lateinit var exoPlayer: ExoPlayer
    private lateinit var videoTitle: TextView
    private lateinit var videoCover: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_play)

        playerView = findViewById(R.id.player_view)
        videoTitle = findViewById(R.id.video_title)
        videoCover = findViewById(R.id.video_cover)

        val videoUrl = intent.getStringExtra("VIDEO_URL")
        val title = intent.getStringExtra("VIDEO_TITLE")
        val coverUrl = intent.getStringExtra("VIDEO_COVER_URL")

        videoTitle.text = title
//        Picasso.get().load(coverUrl).into(videoCover)

        initializePlayer(videoUrl)
    }

    private fun initializePlayer(videoUrl: String?) {
        exoPlayer = ExoPlayer.Builder(this).build()
        playerView.player = exoPlayer

        val mediaItem = MediaItem.fromUri(Uri.parse(videoUrl))
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
        exoPlayer.playWhenReady = true
    }

    override fun onStop() {
        super.onStop()
        exoPlayer.release()
    }

}
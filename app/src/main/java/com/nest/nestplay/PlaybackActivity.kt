package com.nest.nestplay

import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.leanback.app.VideoSupportFragment
import androidx.leanback.app.VideoSupportFragmentGlueHost
import com.nest.nestplay.model.MovieModel
import com.nest.nestplay.player.BasicMediaPlayerAdpter
import com.nest.nestplay.player.CustomTransportControlGlue

class PlaybackFragment : VideoSupportFragment() {

    private lateinit var transporGlue: CustomTransportControlGlue

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val data = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getParcelable("movie", MovieModel::class.java)
        } else {
            arguments?.getParcelable<MovieModel>("movie")
        }

        transporGlue = CustomTransportControlGlue(
            context = requireContext(),
            playerAdpter = BasicMediaPlayerAdpter(requireContext())
        )

        transporGlue.host = VideoSupportFragmentGlueHost(this)

        transporGlue.loadMovieInfo(data)

        transporGlue.playerAdapter.setDataSource(Uri.parse(data?.url))
        transporGlue.playerAdapter.play()
        setOnKeyInterceptListener { view, keyCode, event ->
            if(isControlsOverlayVisible || event.repeatCount > 0 ){

            } else when(keyCode){

            }
            transporGlue.onKey(view, keyCode, event)
        }
    }

}

package com.nest.nestplay

import android.os.Build
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import com.nest.nestplay.model.MovieModel

class VideoPlayActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_playback)


        val data = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("movie", MovieModel::class.java )
        } else {
            intent.getParcelableExtra<MovieModel>("movie")
        }

        var fragment = PlaybackFragment()
        val bundle = Bundle()
        bundle.putParcelable("movie", data)

        fragment.arguments = bundle

        if(savedInstanceState == null){
            supportFragmentManager.beginTransaction()
                .replace(android.R.id.content, fragment)
                .commit()
        }
    }
}
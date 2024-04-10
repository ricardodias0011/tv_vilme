package com.nest.nestplay.player

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.view.KeyEvent
import android.view.View
import androidx.leanback.media.PlaybackTransportControlGlue
import androidx.leanback.widget.Action
import androidx.leanback.widget.ArrayObjectAdapter
import androidx.leanback.widget.PlaybackControlsRow
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.nest.nestplay.model.Genres
import com.nest.nestplay.model.MovieModel

class CustomTransportControlGlue(
    context: Context,
    playerAdpter: BasicMediaPlayerAdpter
): PlaybackTransportControlGlue<BasicMediaPlayerAdpter>(context, playerAdpter) {
    private val forwardAction = PlaybackControlsRow.FastForwardAction(context)
    private val rewindAction = PlaybackControlsRow.RewindAction(context)
    private val nextAction = PlaybackControlsRow.SkipNextAction(context)
    private val previousAction = PlaybackControlsRow.SkipPreviousAction(context)

    init {
        isSeekEnabled = true
    }


    override fun onCreatePrimaryActions(primaryActionsAdapter: ArrayObjectAdapter) {
        primaryActionsAdapter.add(previousAction)
        primaryActionsAdapter.add(rewindAction)
        super.onCreatePrimaryActions(primaryActionsAdapter)
        primaryActionsAdapter.add(forwardAction)
        primaryActionsAdapter.add(nextAction)
    }

    override fun onActionClicked(action: Action?) {
        when (action){
            forwardAction -> playerAdapter.fastForward()
            rewindAction -> playerAdapter.rewind()
            else -> super.onActionClicked(action)
        }
        onUpdateProgress()
    }

    override fun onKey(v: View?, keyCode: Int, event: KeyEvent?): Boolean {
        if (event != null) {
            if(host.isControlsOverlayVisible || event.repeatCount > 0){
                return super.onKey(v, keyCode, event)
            }
        }

        return when (keyCode) {
            KeyEvent.KEYCODE_DPAD_RIGHT ->
                if (event?.action != KeyEvent.ACTION_DOWN) false else {
                    onActionClicked(forwardAction)
                    true
                }
            KeyEvent.KEYCODE_DPAD_LEFT ->
                if(event?.action != KeyEvent.ACTION_DOWN) false else {
                    onActionClicked(rewindAction)
                    true
                }
            else -> super.onKey(v, keyCode, event)
        }
    }
    fun loadMovieInfo(movie: MovieModel?) {

        title = movie?.title
        if(movie != null) {
            val genresList = Genres.genres.filter { it?.id in movie.genre_ids }
            val genresNames = genresList.joinToString(" ● ") { "${it.name}" }
            subtitle = "$genresNames - ${movie?.release_date?.slice(0..3)}"
        }

        Glide.with(context)
            .asBitmap()
            .load("https://image.tmdb.org/t/p/w1280" + movie?.backdrop_path)
            .into(object : CustomTarget<Bitmap>(){
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    controlsRow.setImageBitmap(context, resource)
                    host.notifyPlaybackRowChanged()
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                    controlsRow.setImageBitmap(context, null)
                    host.notifyPlaybackRowChanged()
                }

            })

        playWhenPrepared()
    }

}
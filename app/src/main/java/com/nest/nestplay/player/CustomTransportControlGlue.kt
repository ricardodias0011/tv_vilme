package com.nest.nestplay.player

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.view.KeyEvent
import android.view.View
import androidx.core.content.ContextCompat
import androidx.leanback.media.PlaybackGlue
import androidx.leanback.media.PlaybackTransportControlGlue
import androidx.leanback.widget.Action
import androidx.leanback.widget.ArrayObjectAdapter
import androidx.leanback.widget.PlaybackControlsRow
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.nest.nestplay.R
import com.nest.nestplay.model.Genres
import com.nest.nestplay.model.MovieModel

class CustomTransportControlGlue(
    context: Context,
    playerAdpter: BasicMediaPlayerAdpter
): PlaybackTransportControlGlue<BasicMediaPlayerAdpter>(context, playerAdpter) {
    private lateinit var currentMovie: MovieModel

    private val forwardAction = PlaybackControlsRow.FastForwardAction(context)
    private val rewindAction = PlaybackControlsRow.RewindAction(context)

    init {
        if (isPrepared) {
            setSeekProvider(
                PlaybackSeekDiskDataProvider(
                    duration / 100,
                    currentMovie.url,
                    PlaybackSeekDiskDataProvider.path
                )
            )
        } else {
            addPlayerCallback(object : PlayerCallback() {
                override fun onPreparedStateChanged(glue: PlaybackGlue) {
                    if (glue.isPrepared) {
                        glue.removePlayerCallback(this)
                        val transportControlGlue = glue as PlaybackTransportControlGlue<*>
                        transportControlGlue.setSeekProvider(
                            PlaybackSeekDiskDataProvider(
                                duration / 100,
                                currentMovie.url,
                                PlaybackSeekDiskDataProvider.path
                            )
                        )
                    }
                }
            })
        }
        isSeekEnabled = false
    }

    private val qualityChangerAction = object : PlaybackControlsRow.MoreActions(context) {
        init {
            icon = ContextCompat.getDrawable(context, R.drawable.ic_sliders)
        }
    }

    init {
        isSeekEnabled = false
    }

    fun startVideoAtTime(timeMillis: Long) {
        try {
            playerAdapter.seekTo(timeMillis)
            playerAdapter.play()
        } catch (e: Exception) {
            e.printStackTrace()
            playerAdapter.reset()
            playerAdapter.play()
        }
    }

    override fun getCurrentPosition(): Long {
        return playerAdapter.currentPosition
    }

    override fun onCreatePrimaryActions(primaryActionsAdapter: ArrayObjectAdapter) {
        super.onCreatePrimaryActions(primaryActionsAdapter)
    }

    override fun onCreateSecondaryActions(secondaryActionsAdapter: ArrayObjectAdapter?) {
        super.onCreateSecondaryActions(secondaryActionsAdapter)
        if (secondaryActionsAdapter != null) {
        }
    }

    override fun onActionClicked(action: Action?) {
        when (action){
            forwardAction -> {
                val currentTime = getCurrentPosition()
                val desiredTime = currentTime + 10000
                println("CustomTransportControlGlue Avançar para o tempo: $desiredTime")
                if (desiredTime <= playerAdapter.duration) {
                    playerAdapter.seekTo(desiredTime)
                }
            }
            rewindAction -> playerAdapter.rewind()
            else -> super.onActionClicked(action)
        }
        onUpdateProgress()
    }

    override fun onPlayStateChanged() {
        super.onPlayStateChanged()
        if(playerAdapter.isPlaying){
            host?.hideControlsOverlay(true)
        }
    }

    fun hideControlsOverlay(immediate: Boolean) {
        if (immediate) {
            host?.hideControlsOverlay(true)
        } else {
            host?.hideControlsOverlay(false)
        }
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

        if(movie != null){
            currentMovie = movie
        }

        if(movie?.contentType == "Serie"){
            title = movie?.name
            if (movie?.current_ep != 0 || movie?.current_ep != null){
                title = title as String? + " Ep: ${movie?.current_ep}"
            }
        }else{
            title = movie?.title
        }
        if(movie != null) {
            val genresList = Genres.genres.filter { it?.id in movie.genre_ids }
            val genresNames = genresList.joinToString(" ● ") { "${it.name}" }
            var date_release = movie?.release_date
            if(movie?.contentType == "Serie"){
                date_release = movie?.first_air_date
            }
            subtitle = "$genresNames - ${date_release?.slice(0..3)}"
            if(movie?.season != null && movie?.season != 0 && movie?.contentType == "Serie"){
                subtitle = subtitle as String + " (Temp: ${movie?.season})"
            }
        }

        var urlImageBG: String?

        if(movie?.backdrop_path?.startsWith("http") == true){
            urlImageBG = movie?.backdrop_path.toString()
        }
        else{
            urlImageBG = "https://image.tmdb.org/t/p/w780" + movie?.backdrop_path
        }

        println(urlImageBG)
        Glide.with(context)
            .asBitmap()
            .load(urlImageBG)
            .into(object : CustomTarget<Bitmap>(){
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    controlsRow?.setImageBitmap(context, resource)
                    host?.notifyPlaybackRowChanged()
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                    controlsRow?.setImageBitmap(context, null)
                    host?.notifyPlaybackRowChanged()
                }

            })

        playWhenPrepared()
    }

    fun removePlayerCallback() {

    }

}

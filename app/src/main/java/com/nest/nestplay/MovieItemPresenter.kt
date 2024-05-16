package com.nest.nestplay

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.leanback.widget.Presenter
import com.bumptech.glide.Glide
import com.nest.nestplay.model.MovieModel
class MovieItemPresenter : Presenter() {
    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {

        val view =
            LayoutInflater.from(parent?.context).inflate(R.layout.movie_item, parent, false)

        val params = view.layoutParams
        params.width = getWidthInPercent(parent!!.context, 12)
        params.height = getHeightInPercent(parent!!.context, 30)

        view.setOnFocusChangeListener { view, hasFocus ->
            if (hasFocus) {
                view.setBackgroundResource(R.drawable.item_selected_background)
            } else {
                view.background = null
            }
        }

        return ViewHolder(view)

    }

    fun getWidthInPercent(context: Context, percent: Int): Int {
        val width = context.resources.displayMetrics.widthPixels ?: 0
        return (width * percent) / 100
    }

    fun getHeightInPercent(context: Context, percent: Int): Int {
        val width = context.resources.displayMetrics.heightPixels ?: 0
        return (width * percent) / 100
    }


    override fun onBindViewHolder(viewHolder: ViewHolder, item: Any?) {
        val content = item as MovieModel

        val imageview = viewHolder!!.view.findViewById<ImageView>(R.id.posteritemmovie)

        val path = content.poster_path
        Glide.with(viewHolder.view.context)
            .load(path)
            .into(imageview)

    }

    override fun onUnbindViewHolder(viewHolder: ViewHolder) {
    }
}


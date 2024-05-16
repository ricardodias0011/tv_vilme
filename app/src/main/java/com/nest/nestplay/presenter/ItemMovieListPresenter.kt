package com.nest.nestplay.presenter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.leanback.widget.Presenter
import com.bumptech.glide.Glide
import com.nest.nestplay.R
import com.nest.nestplay.model.ListMovieModel

class MovieListItemPresenter : Presenter() {
    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {

        val view =
            LayoutInflater.from(parent?.context).inflate(R.layout.movie_item, parent, false)

        val params = view.layoutParams
        params.width = getWidthInPercent(parent!!.context, 14)
        params.height = getHeightInPercent(parent!!.context, 34)
        val roundedBackgroundDrawable = ContextCompat.getDrawable(parent.context, R.drawable.item_movie_bg)
        view.background =roundedBackgroundDrawable
        view.setOnFocusChangeListener { view, hasFocus ->
            if (hasFocus) {
                view.setBackgroundResource(R.drawable.item_selected_background)
            } else {
                view.background = roundedBackgroundDrawable
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

        val content = item as? ListMovieModel.Movie

        val imageview = viewHolder!!.view.findViewById<ImageView>(R.id.posteritemmovie)
        if(imageview != null){
            val url = content?.poster_path
            Glide.with(viewHolder?.view?.context!!)
                .load(url)
                .into(imageview!!)
        }


    }

    override fun onUnbindViewHolder(viewHolder: ViewHolder) {
    }
}
package com.nest.nestplay.adpters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.annotation.AnimRes
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.nest.nestplay.DetailMovieActivity
import com.nest.nestplay.R
import com.nest.nestplay.databinding.MovieItemBinding
import com.nest.nestplay.model.ListMovieModel
import com.nest.nestplay.utils.Common.Companion.getHeightInPercent
import com.nest.nestplay.utils.Common.Companion.getWidthInPercent

class MoviesListAdpter(private val context: Context, private val listMovies: MutableList<ListMovieModel.Movie>):
    RecyclerView.Adapter<MoviesListAdpter.MovieViewHolder>() {

    var onItemFocusChangeListener: OnItemFocusChangeListener? = null

    inner class MovieViewHolder(binding: MovieItemBinding): RecyclerView.ViewHolder(binding.root){
        val poster = binding.posteritemmovie

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovieViewHolder {
        val itemList = MovieItemBinding.inflate(LayoutInflater.from(context), parent, false)
        return MovieViewHolder(itemList)
    }

    override fun getItemCount() = listMovies.size

    override fun onBindViewHolder(holder: MovieViewHolder, position: Int) {
        holder.itemView.setBackgroundResource(R.drawable.no_selected_bg)
        holder.itemView.setOnFocusChangeListener { view, hasFocus ->
            if (hasFocus) {
                val movie = listMovies[position]
                onItemFocusChangeListener?.onItemFocused(movie)

                holder.itemView.setBackgroundResource(R.drawable.item_selected_background)
            } else {
                holder.itemView.setBackgroundResource(R.drawable.no_selected_bg)
            }
        }

        holder.itemView.setOnClickListener { view ->
            val movie = listMovies[position]
            val intent = Intent(view.context, DetailMovieActivity::class.java)
            intent.putExtra("id", movie.id)
            view.context.startActivity(intent)
        }

        val layoutParams = holder.poster.layoutParams

        layoutParams.width = getWidthInPercent(context, 15)
        layoutParams.height = getHeightInPercent(context, 38)
        holder.poster.layoutParams = layoutParams
        Glide.with(context)
            .load(listMovies[position].poster_path!!)
            .into(holder.poster)
    }

    private fun animateItem(view: View, @AnimRes animationResId: Int) {
        val animation = AnimationUtils.loadAnimation(context, animationResId)
        view.startAnimation(animation)
    }

    interface OnItemFocusChangeListener {
        fun onItemFocused(movie: ListMovieModel.Movie)
    }
}

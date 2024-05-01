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
    private var focusedItemPosition = RecyclerView.NO_POSITION
    private var focusItem: View? = null
    var onItemFocusChangeListener: OnItemFocusChangeListener? = null
    var onLastItemFocusChangeListener: OnLastItemFocusChangeListener? = null

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

        if (position == focusedItemPosition) {
            holder.itemView.requestFocus()
            holder.itemView.setBackgroundResource(R.drawable.item_selected_background)
        }

        holder.itemView.setOnFocusChangeListener { view, hasFocus ->
            val movie = listMovies[position]
            val isLastItem = position >= listMovies.size - 5
            if(isLastItem){
                if (hasFocus) {
                    focusItem = holder.itemView
                    focusedItemPosition = holder.adapterPosition
                    onLastItemFocusChangeListener?.onLastItemFocused(movie)
                }
            }
            if (hasFocus) {

                onItemFocusChangeListener?.onItemFocused(movie, holder.adapterPosition)

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

    fun onFocusItem() {
        println("focusItem.requestFocus")
        println(focusItem)
        focusItem?.requestFocus()
    }


    interface OnItemFocusChangeListener {
        fun onItemFocused(movie: ListMovieModel.Movie, itemView: Int)
    }

    interface OnLastItemFocusChangeListener {
        fun onLastItemFocused(movie: ListMovieModel.Movie)
    }
}
